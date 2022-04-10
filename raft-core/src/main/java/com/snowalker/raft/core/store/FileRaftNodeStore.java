package com.snowalker.raft.core.store;

import com.google.common.io.Files;
import com.snowalker.raft.common.exception.NodeStoreException;
import com.snowalker.raft.common.store.RandomAccessFileDelegator;
import com.snowalker.raft.common.store.SeekableFile;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 19:36
 * @desc RaftNodeStore文件实现
 */
public class FileRaftNodeStore implements RaftNodeStore {

	private static final String FILE_NAME = "node.bin";
	private static final long OFFSET_TERM = 0;
	private static final long OFFSET_VOTED_FOR = 4;
//	private final RandomAccessFile randomAccessFile;
	private final SeekableFile seekableFile;
	private int currentTerm = 0;
	private RaftNodeId votedFor = null;

	public FileRaftNodeStore(File file) {
		try {
			// 如果文件不存在则创建
			if (!file.exists()) {
				// 创建一个空文件或更新上次更新的时间戳，与同名的 unix 命令相同
				Files.touch(file);
			}
			seekableFile =  new RandomAccessFileDelegator(file);
			initializeOrLoadFile();
			// r	以只读的方式打开文本，也就意味着不能用write来操作文件
			// rw	读操作和写操作都是允许的
			// rws	每当进行写操作，同步的刷新到磁盘，刷新内容和元数据
			// rwd	每当进行写操作，同步的刷新到磁盘，刷新内容
//			randomAccessFile = new RandomAccessFile(file, "rws");

		} catch (IOException e) {
			throw new NodeStoreException(e);
		}
	}

	private void initializeOrLoadFile() {
	}

	/**
	 * 获取currentTerm
	 */
	@Override
	public int getCurrentTerm() {
		return 0;
	}

	/**
	 * 设置currentTerm
	 *
	 * @param currentTerm
	 */
	@Override
	public void setCurrentTerm(int currentTerm) {

	}

	/**
	 * 获取votedFor
	 */
	@Override
	public RaftNodeId getVotedFor() {
		return null;
	}

	/**
	 * 设置votedFor
	 *
	 * @param votedFor
	 */
	@Override
	public void setVotedFor(RaftNodeId votedFor) {

	}

	/**
	 * 关闭文件
	 */
	@Override
	public void close() {

	}
}
