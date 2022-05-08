package com.snowalker.raft.core.store.support;

import java.io.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 21:15
 * @desc TODO RandomAccessFile适配类
 */
public class RandomAccessFileDelegator implements SeekableFile {

	private final File file;

	private final RandomAccessFile randomAccessFile;

	public RandomAccessFileDelegator(File file) throws FileNotFoundException {
		this(file, "rw");
	}

	/**
	 * 指定mode
	 * @param file
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public RandomAccessFileDelegator(File file, String mode) throws FileNotFoundException {
		this.file = file;
		randomAccessFile = new RandomAccessFile(file, mode);
	}

	/**
	 * 移动到执行的位置
	 *
	 * @param position
	 */
	@Override
	public void seek(long position) throws IOException {
		randomAccessFile.seek(position);
	}

	/**
	 * 写入整型
	 *
	 * @param i
	 */
	@Override
	public void writeInt(int i) throws IOException {
		randomAccessFile.writeInt(i);
	}

	/**
	 * 写入长整型
	 *
	 * @param l
	 */
	@Override
	public void writeLong(long l) throws IOException {
		randomAccessFile.writeLong(l);
	}

	/**
	 * 写入字节数组
	 *
	 * @param bytes
	 */
	@Override
	public void write(byte[] bytes) throws IOException {
		randomAccessFile.write(bytes);
	}

	/**
	 * 读取int
	 */
	@Override
	public int readInt() throws IOException {
		return randomAccessFile.readInt();
	}

	/**
	 * 读取long
	 */
	@Override
	public long readLong() throws IOException {
		return randomAccessFile.readLong();
	}

	/**
	 * 读取字节数组，返回读取长度
	 *
	 * 从此文件中读取最多b.length个字节的数据到字节数组中。
	 * 此方法会阻塞，直到至少有一个字节的输入可用。
	 * 尽管RandomAccessFile不是InputStream的子类，
	 * 但此方法的行为方式与 InputStream 的InputStream InputStream.read(byte[])方法完全相同。
	 *
	 * @param bytes
	 */
	@Override
	public int read(byte[] bytes) throws IOException {
		return randomAccessFile.read(bytes);
	}

	/**
	 * 获取文件大小
	 */
	@Override
	public long size() throws IOException {
		return randomAccessFile.length();
	}

	/**
	 * 裁减到指定大小
	 *
	 * @param size
	 */
	@Override
	public void truncate(long size) throws IOException {
		randomAccessFile.setLength(size);
	}

	/**
	 * 获取从指定位置开始的输入流
	 * 跳过并丢弃输入流中的n字节数据
	 * @param beginOffset
	 */
	@Override
	public InputStream inputStream(long beginOffset) throws IOException {
		FileInputStream input = new FileInputStream(file);
		if (beginOffset > 0) {
			input.skip(beginOffset);
		}
		return input;
	}

	/**
	 * 强制刷盘
	 */
	@Override
	public void flush() throws IOException {
	}

	/**
	 * 获取当前位置, 返回此文件中的当前偏移量。
	 * return：
	 *      从文件开头的偏移量，以字节为单位，发生下一次读取或写入。
	 */
	@Override
	public long position() throws IOException {
		return randomAccessFile.getFilePointer();
	}

	/**
	 * 关闭文件
	 */
	@Override
	public void close() throws IOException {
		randomAccessFile.close();
	}
}
