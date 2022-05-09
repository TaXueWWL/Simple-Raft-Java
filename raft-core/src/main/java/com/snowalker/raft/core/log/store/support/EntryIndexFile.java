package com.snowalker.raft.core.log.store.support;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/8 18:46
 * @desc 日志entry索引文件
 */
public class EntryIndexFile implements Iterable<EntryIndexItem> {

	/**最大条目索引的偏移*/
	private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;

	/**单条日志条目元信息长度*/
	private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

	private final SeekableFile seekableFile;

	/**日志条目数量*/
	private int entryIndexCount;

	/**最小日志索引*/
	private int minEntryIndex;

	/**最大日志索引*/
	private int maxEntryIndex;

	private Map<Integer, EntryIndexItem> entryIndexMap = Maps.newHashMap();

	public EntryIndexFile(SeekableFile seekableFile) throws IOException {
		this.seekableFile = seekableFile;
	}

	public EntryIndexFile(File file) throws IOException {
		this(new RandomAccessFileDelegator(file));
	}

	/**
	 * 加载所有的日志元信息
	 * @throws IOException
	 */
	public void load() throws IOException {
		// 文件长度为0不用加载
		if (seekableFile.size() == 0L) {
			entryIndexCount = 0;
			return;
		}

		minEntryIndex = seekableFile.readInt();
		maxEntryIndex = seekableFile.readInt();

		updateEntryIndexCount();

		// 逐条加载
		long offset = 0L;
		int kind = 0;
		int term = 0;

		for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
			offset = seekableFile.readLong();
			kind = seekableFile.readInt();
			term = seekableFile.readInt();
			entryIndexMap.put(i, new EntryIndexItem(i, offset, kind, term));
		}
	}

	/**
	 * 更新日志条目数量
	 */
	private void updateEntryIndexCount() {
		entryIndexCount = maxEntryIndex - minEntryIndex + 1;
	}

	/**
	 * 追加日志条目元信息  只允许顺序追加
	 * @param index
	 * @param offset
	 * @param kind
	 * @param term
	 * @throws IOException
	 */
	public void appendEntryIndex(int index, int offset, int kind, int term) throws IOException {
		if (seekableFile.size() == 0L) {
			seekableFile.writeInt(index);
			minEntryIndex = index;
		} else {
			int currentIndex = maxEntryIndex + 1;
			if (index != currentIndex) {
				throw new IllegalArgumentException("index must be :" + currentIndex + ", but was:" + index);
			}
			// 跳过minEntryIndex
			seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
		}

		// 写入maxEntryIndex
		seekableFile.writeInt(index);
		maxEntryIndex = index;
		updateEntryIndexCount();

		// 移动到文件末尾
		seekableFile.seek(getOffsetOfEntryIndexItem(index));
		seekableFile.writeLong(offset);
		seekableFile.writeInt(kind);
		seekableFile.writeInt(term);
		entryIndexMap.put(index, new EntryIndexItem(index, offset, kind, term));
	}

	/**
	 * 获取指定index的日志偏移量
	 * 偏移量 = （当前index - 最小index） * 16（long型字节数）+ 2 * int 字节数
	 * @param index
	 * @return
	 */
	private long getOffsetOfEntryIndexItem(int index) {
		return (long) (index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2;
	}

	/**
	 * 移除某个索引之后的数据
	 * @param newMaxEntryIndex
	 * @throws IOException
	 */
	public void removeAfter(int newMaxEntryIndex) throws IOException {

		if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) {
			return;
		}

		// 判断新的maxEntryIndex是否比minEntryIndex小
		// 如果是的话则全部移除
		if (newMaxEntryIndex < minEntryIndex) {
			clear();
			return;
		}

		// 修改maxEntryIndex
		seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
		seekableFile.writeInt(newMaxEntryIndex);
		// 裁减文件
		seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1));
		// 移除缓存的元信息
		for (int i = newMaxEntryIndex + 1; i <= maxEntryIndex; i++) {
			entryIndexMap.remove(i);
		}

		maxEntryIndex = newMaxEntryIndex;
		entryIndexCount = newMaxEntryIndex - minEntryIndex + 1;
	}

	/**
	 * 清理索引文件
	 * @throws IOException
	 */
	public void clear() throws IOException {
		seekableFile.truncate(0L);
		entryIndexCount = 0;
		entryIndexMap.clear();
	}

	public boolean isEmpty() {
		return entryIndexCount == 0;
	}

	public int getMinEntryIndex() {
		checkEmpty();
		return minEntryIndex;
	}

	private void checkEmpty() {
		if (isEmpty()) {
			throw new IllegalStateException("no entry index");
		}
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@NotNull
	@Override
	public Iterator<EntryIndexItem> iterator() {
		if (isEmpty()) {
			return Collections.emptyIterator();
		}
		return new EntryIndexIterator(entryIndexCount, minEntryIndex);
	}

	private class EntryIndexIterator implements Iterator<EntryIndexItem> {

		/**条目总数*/
		private final int entryIndexCount;
		/**当前索引值*/
		private int currentEntryIndex;

		EntryIndexIterator(int entryIndexCount, int minEntryIndex) {
			this.entryIndexCount = entryIndexCount;
			this.currentEntryIndex = minEntryIndex;
		}

		/**
		 * 是否存在下一条
		 * @return
		 */
		@Override
		public boolean hasNext() {
			checkModification();
			return currentEntryIndex <= maxEntryIndex;
		}

		/**
		 * 检查是否被修改
		 */
		private void checkModification() {
			if (this.entryIndexCount != EntryIndexFile.this.entryIndexCount) {
				throw new IllegalStateException("entry index count changed");
			}
		}


		/**
		 * 获取下一条
		 * @return
		 */
		@Override
		public EntryIndexItem next() {
			checkModification();
			return entryIndexMap.get(currentEntryIndex++);
		}
	}
}
