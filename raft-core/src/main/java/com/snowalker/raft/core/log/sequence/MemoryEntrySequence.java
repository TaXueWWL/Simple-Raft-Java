package com.snowalker.raft.core.log.sequence;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.snowalker.raft.core.log.LogEntry;

import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/3 19:43
 * @desc 内存日志序列实现
 * 对于MemoryEntrySequence而言，日志条目追加时，就等于已经提交
 * 所以commit并不会做更多的事情
 * 同样的，close也不需要做额外的事情
 */
public class MemoryEntrySequence extends AbstractEntrySequence {

	private final List<LogEntry> entries = Lists.newArrayList();

	private int commitIndex = 0;

	public MemoryEntrySequence() {
		this(1);
	}

	public MemoryEntrySequence(int logIndexOffset) {
		super(logIndexOffset);
	}

	/**
	 * 如何获取日志内容，由具体的子类实现
	 *
	 * @param index
	 * @return
	 */
	@Override
	protected LogEntry doGetEntry(int index) {
		return entries.get(index - logIndexOffset);
	}

	/**
	 * 获取日志子序列  需要注意处理已有的偏移量
	 * 如：偏移量为0 则from 为 from - 0， to 为 to - 0
	 * 如果偏移量为10 则from为 from - 10，to 为 to - 10
	 *
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Override
	protected List<LogEntry> doSubList(int fromIndex, int toIndex) {
		return entries.subList(fromIndex - logIndexOffset, toIndex - logIndexOffset);
	}

	/**
	 * 追加日志实现
	 *
	 * @param entry
	 */
	@Override
	protected void doAppend(LogEntry entry) {
		entries.add(entry);
	}

	@Override
	public void commit(int index) {
		commitIndex = index;
	}

	@Override
	public int getCommitIndex() {
		return commitIndex;
	}

	/**
	 * 移除某个索引之后的日志（方便快速进行日志的截取重放）
	 *
	 * @param index
	 */
	@Override
	protected void doRemoveAfter(int index) {
		if (index < doGetFirstLogIndex()) {
			// 清空列表并初始化下一个日志序列为初始化的偏移量
			entries.clear();
			nextLogIndex = logIndexOffset;
		} else {
			entries.subList(index - logIndexOffset + 1, entries.size()).clear();
			nextLogIndex = index + 1;
		}
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("logIndexOffset", logIndexOffset)
				.add("nextLogIndex", nextLogIndex)
				.add("commitIndex", commitIndex)
				.add("entries.size()", entries.size())
				.toString();
	}
}
