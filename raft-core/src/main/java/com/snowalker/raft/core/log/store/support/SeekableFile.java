package com.snowalker.raft.core.log.store.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 21:08
 * @desc 文件操作抽象
 */
public interface SeekableFile {

	/**获取当前位置*/
	long position() throws IOException;

	/**移动到执行的位置*/
	void seek(long position) throws IOException;

	/**写入整型*/
	void writeInt(int i) throws IOException;

	/**写入长整型*/
	void writeLong(long l) throws IOException;

	/**写入字节数组*/
	void write(byte[] bytes) throws IOException;

	/**读取int*/
	int readInt() throws IOException;

	/**读取long*/
	long readLong() throws IOException;

	/**读取字节数组，返回读取长度*/
	int read(byte[] bytes) throws IOException;

	/**获取文件大小*/
	long size() throws IOException;

	/**裁减到指定大小*/
	void truncate(long size) throws IOException;

	/**获取从指定位置开始的输入流*/
	InputStream inputStream(long beginOffset) throws IOException;

	/**强制刷盘*/
	void flush() throws IOException;

	/**关闭文件*/
	void close() throws IOException;
}
