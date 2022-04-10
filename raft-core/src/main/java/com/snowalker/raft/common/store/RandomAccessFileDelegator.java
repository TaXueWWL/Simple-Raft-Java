package com.snowalker.raft.common.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 21:15
 * @desc TODO RandomAccessFile适配类
 */
public class RandomAccessFileDelegator implements SeekableFile {

	private final File file;

	public RandomAccessFileDelegator(File file) {
		this.file = file;
	}

	/**
	 * 获取当前位置
	 */
	@Override
	public long position() throws IOException {
		return 0;
	}

	/**
	 * 移动到执行的位置
	 *
	 * @param position
	 */
	@Override
	public void seek(long position) throws IOException {

	}

	/**
	 * 写入整型
	 *
	 * @param i
	 */
	@Override
	public void writeInt(int i) throws IOException {

	}

	/**
	 * 写入长整型
	 *
	 * @param l
	 */
	@Override
	public void writeLong(long l) throws IOException {

	}

	/**
	 * 写入字节数组
	 *
	 * @param bytes
	 */
	@Override
	public void write(byte[] bytes) throws IOException {

	}

	/**
	 * 读取int
	 */
	@Override
	public int readInt() throws IOException {
		return 0;
	}

	/**
	 * 读取long
	 */
	@Override
	public long readLong() throws IOException {
		return 0;
	}

	/**
	 * 读取字节数组，返回读取长度
	 *
	 * @param bytes
	 */
	@Override
	public int read(byte[] bytes) throws IOException {
		return 0;
	}

	/**
	 * 获取文件大小
	 */
	@Override
	public long size() throws IOException {
		return 0;
	}

	/**
	 * 裁减到指定大小
	 *
	 * @param size
	 */
	@Override
	public void truncate(long size) throws IOException {

	}

	/**
	 * 获取从指定位置开始的输入流
	 *
	 * @param beginOffset
	 */
	@Override
	public InputStream inputStream(long beginOffset) throws IOException {
		return null;
	}

	/**
	 * 强制刷盘
	 */
	@Override
	public void flush() throws IOException {

	}

	/**
	 * 关闭文件
	 */
	@Override
	public void close() throws IOException {

	}
}
