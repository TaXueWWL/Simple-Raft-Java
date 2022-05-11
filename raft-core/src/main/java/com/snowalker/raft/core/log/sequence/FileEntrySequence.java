package com.snowalker.raft.core.log.sequence;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.snowalker.raft.common.exception.LogException;
import com.snowalker.raft.core.log.LogDir;
import com.snowalker.raft.core.log.LogEntry;
import com.snowalker.raft.core.log.LogEntryFactory;
import com.snowalker.raft.core.log.LogEntryMeta;
import com.snowalker.raft.core.log.store.support.EntriesFile;
import com.snowalker.raft.core.log.store.support.EntryIndexFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/11 21:32
 * @desc
 */
@Slf4j
public class FileEntrySequence extends AbstractEntrySequence {

	private final LogEntryFactory entryFactory = LogEntryFactory.getInstance();
	private final EntriesFile entriesFile;
	private final EntryIndexFile entryIndexFile;
	private final LinkedList<LogEntry> pendingEntries = Lists.newLinkedList();

	/**
	 * Raft算法中定义初始的commitIndex为0，和日志是否持久化无关
	 */
	private int commitIndex = 0;

	public FileEntrySequence(LogDir logDir, int logIndexOffset) {

		// 默认logIndexOffset由外部决定
		super(logIndexOffset);
		try {
			this.entriesFile = new EntriesFile(logDir.getEntriesFile());
			this.entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
			init();
		} catch (IOException e) {
			throw new LogException("Failed to open entries file or entry index file.", e);
		}
	}

	public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexItems, int logIndexOffset) {
		super(logIndexOffset);
		this.entriesFile = entriesFile;
		this.entryIndexFile = entryIndexItems;
		init();
	}


	/**
	 * 初始化逻辑
	 */
	private void init() {
		if (entryIndexFile.isEmpty()) {
			log.warn("entryIndexFile.isEmpty(), return without init.");
			return;
		}
		// 使用日志索引文件的minEntryIndex作为logIndexOffset
		logIndexOffset = entryIndexFile.getMinEntryIndex();
		// 使用日志索引文件的MaxEntryIndex + 1作为nextLogIndex
		nextLogIndex = entryIndexFile.getMaxEntryIndex() + 1;
	}

	/**
	 * 获取commitIndex
	 *
	 * @return
	 */
	public int getCommitIndex() {
		return commitIndex;
	}


	/**
	 * 如何获取日志内容，由具体的子类实现
	 *
	 * @param index
	 * @return
	 */
	@Override
	protected LogEntry doGetEntry(int index) {
		if (!pendingEntries.isEmpty()) {
			int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
			if (index >= firstPendingEntryIndex) {
				// 当前索引大于等于第一个
				return pendingEntries.get(index - firstPendingEntryIndex);
			}
		}
		Preconditions.checkArgument(!entryIndexFile.isEmpty());
		return getEntryInFile(index);
	}

	/**
	 * 获取对应index的日志元数据
	 *
	 * @param index
	 * @return
	 */
	public LogEntryMeta getEntryMeta(int index) {
		if (!isEntryPresent(index)) {
			return null;
		}
		if (!pendingEntries.isEmpty()) {
			int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
			if (index >= firstPendingEntryIndex) {
				return pendingEntries.get(index - firstPendingEntryIndex).getMeta();
			}
		}
		return entryIndexFile.get(index).toEntryMeta();
	}

	/**
	 * 按照索引获取文件中的日志条目
	 *
	 * @param index
	 * @return
	 */
	private LogEntry getEntryInFile(int index) {
		long offset = entryIndexFile.getOffset(index);
		try {
			return entriesFile.loadEntry(offset, entryFactory);
		} catch (IOException e) {
			throw new LogException("Failed to load entry :" + index, e);
		}
	}

	public LogEntry getLastEntry() {
		if (isEmpty()) {
			return null;
		}
		if (!pendingEntries.isEmpty()) {
			return pendingEntries.getLast();
		}
		// entry索引文件不为空，则获取当前最大索引对应的文件
		Preconditions.checkArgument(!entryIndexFile.isEmpty());
		return getEntryInFile(entryIndexFile.getMaxEntryIndex());
	}

	/**
	 * 获取日志子序列
	 *
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Override
	protected List<LogEntry> doSubList(int fromIndex, int toIndex) {

		// 结果分为来自文件的与来自缓冲的两部分
		List<LogEntry> result = Lists.newArrayList();

		// 从文件中获取日志条目
		if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.getMaxEntryIndex()) {
			int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex() + 1, toIndex);
			for (int i = fromIndex; i < maxIndex; i++) {
				result.add(getEntryInFile(i));
			}
		}

		// 从日志缓冲中获取日志条目
		if (!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()) {
			Iterator<LogEntry> iterator = pendingEntries.iterator();
			LogEntry entry;
			int index;
			while (iterator.hasNext()) {
				entry = iterator.next();
				index = entry.getIndex();
				if (index >= toIndex) {
					break;
				}
				if (index >= fromIndex) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * 追加日志实现
	 * 直接写缓冲区，刷盘通过commit实现
	 *
	 * @param entry
	 */
	@Override
	protected void doAppend(LogEntry entry) {
		pendingEntries.add(entry);
	}

	/**
	 * 移除某个索引之后的日志（方便快速进行日志的截取重放）
	 * <p>
	 * 在该方法中，需要判断移除的日志索引是否在日志缓冲区中，如果在，
	 * 那么就只需要从后往前移除日志缓冲区中的部分日志即可；
	 * <p>
	 * 否则就需整体清空日志缓冲，并且文件本身还需要进行裁剪
	 *
	 * @param index
	 */
	@Override
	protected void doRemoveAfter(int index) {
		// 只需要移除缓冲中的日志
		if (!pendingEntries.isEmpty() && index >= pendingEntries.getFirst().getIndex() - 1) {
			// 移除指定数量的日志条目
			// 循环方向从小到大 但是移除是从后往前
			// 最终移除指定数量的日志条目
			for (int i = index + 1; i <= doGetLastLogIndex(); i++) {
				// 不断移除最后一个元素
				pendingEntries.removeLast();
			}
			nextLogIndex = index + 1;
			return;
		}

		try {
			if (index >= doGetFirstLogIndex()) {
				// 索引比日志缓冲区中的第一条日志小
				pendingEntries.clear();     // 缓冲区清空
				entriesFile.truncate(entryIndexFile.getOffset(index + 1));
				entryIndexFile.removeAfter(index);
				nextLogIndex = index + 1;
				commitIndex = index;
			} else {
				// 索引比第一条日志的索引都小，则清理掉所有的数据
				pendingEntries.clear();
				entryIndexFile.clear();
				entriesFile.clear();
				nextLogIndex = logIndexOffset;
				commitIndex = logIndexOffset - 1;
			}
		} catch (IOException e) {
			throw new LogException(e);
		}
	}

	/**
	 * 提交index对应日志
	 *
	 * @param index
	 */
	public void commit(int index) {

		// 待提交index 小于 已提交的index，异常抛出
		if (index < commitIndex) {
			throw new IllegalArgumentException("Commit index < " + commitIndex);
		}

		// 提交过了不需要重复提交
		if (index == commitIndex) {
			return;
		}

		// 如果commitIndex在文件内，则只需要更新commitIndex
		if (!entryIndexFile.isEmpty() && index <= entryIndexFile.getMaxEntryIndex()) {
			commitIndex = index;
			return;
		}

		// 检查commitIndex是否在日志缓冲的区间内 (以下条件为commitIndex不在缓冲区内)
		if (pendingEntries.isEmpty() ||
				pendingEntries.getFirst().getIndex() > index ||
				pendingEntries.getLast().getIndex() < index) {
			throw new IllegalArgumentException("No entry to commit or commitIndex exceed");
		}

		long offset;
		LogEntry entry = null;

		// 将缓冲区的entry追加到文件中，移除缓冲区entry，更新所以文件以及已提交的日志索引commitIndex
		// 这里的操作为将缓冲区中的entry全部刷盘
		try {
			for (int i = pendingEntries.getFirst().getIndex(); i <= index; i++) {
				entry = pendingEntries.removeFirst();
				offset = entriesFile.appendEntry(entry);
				entryIndexFile.appendEntryIndex(i, offset, entry.getKind(), entry.getTerm());
				commitIndex = i;
			}
		} catch (IOException e) {
			throw new LogException("Failed to commit entry :" + entry, e);
		}
	}
}
