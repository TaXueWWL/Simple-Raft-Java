package com.snowalker.raft.core.store.support;

import com.snowalker.raft.core.log.LogEntry;
import com.snowalker.raft.core.log.LogEntryFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/8 18:19
 * @desc
 */
public class EntriesFile {

	private final SeekableFile seekableFile;

	public EntriesFile(File file) throws FileNotFoundException {
		this(new RandomAccessFileDelegator(file));
	}

	public EntriesFile(SeekableFile seekableFile) {
		this.seekableFile = seekableFile;
	}

	/**
	 * 追加日志，格式如下
	 *     int(4)    int(4)     int(4)   int(4)    byte
	 *     kind      index      term     length    command bytes
	 * @param entry
	 * @return
	 * @throws IOException
	 */
	public long appendEntry(LogEntry entry) throws IOException {
		long offset = seekableFile.size();

		// 移动到offset位置
		seekableFile.seek(offset);

		seekableFile.writeInt(entry.getKind());
		seekableFile.writeInt(entry.getIndex());
		seekableFile.writeInt(entry.getTerm());

		// 日志内容
		byte[] commandBytes = entry.getCommandBytes();
		seekableFile.writeInt(commandBytes.length);
		seekableFile.write(commandBytes);

		return offset;
	}

	/**
	 * 从指定的offset位置加载日志条目
	 * @param offset
	 * @param logEntryFactory
	 * @throws IOException
	 */
	public LogEntry loadEntry(long offset, LogEntryFactory logEntryFactory) throws IOException {

		if (offset > seekableFile.size()) {
			// 错误的文件size
			throw new IllegalArgumentException("offset > seekableFile.size()!");
		}

		// 移动到offset位置
		seekableFile.seek(offset);

		int kind = seekableFile.readInt();
		int index = seekableFile.readInt();
		int term = seekableFile.readInt();
		int length = seekableFile.readInt();

		// 读取日志内容
		byte[] commandBytes = new byte[length];
		seekableFile.read(commandBytes);

		return logEntryFactory.create(kind, index, term, commandBytes);
	}

	/**
	 * 获取文件大小
	 * @return
	 * @throws IOException
	 */
	public long size() throws IOException {
		return seekableFile.size();
	}

	/**
	 * 清空文件内容
	 * @throws IOException
	 */
	public void clear() throws IOException {
		truncate(0);
	}

	/**
	 * 从指定的偏移量 裁减文件到指定大小
	 * @param offset
	 * @throws IOException
	 */
	public void truncate(long offset) throws IOException {
		seekableFile.truncate(offset);
	}

	public void close() throws IOException {
		seekableFile.close();
	}
}
