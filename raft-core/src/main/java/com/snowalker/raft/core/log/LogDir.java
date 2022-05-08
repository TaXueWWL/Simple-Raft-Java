package com.snowalker.raft.core.log;

import java.io.File;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/8 18:47
 * @desc 获取指定文件地址的接口
 */
public interface LogDir {

	/**
	 * 初始化某个目录
	 */
	void initialize();
	/**
	 * 是否存在该目录
	 */
	boolean exists();
	/**
	 * 获取EntriesFile对应的文件
	 */
	File getEntriesFile();
	/**
	 * 回去EntryIndexFile对应的文件
	 */
	File getEntryOffsetIndexFile();
	/**
	 * 获取某个目录
	 */
	File get();
	/**
	 * 重命名目录
	 */
	boolean renameTo(LogDir logDir);
}
