package com.snowalker.raft.core.store;

import com.google.common.io.Files;
import com.snowalker.raft.common.exception.NodeStoreException;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.store.support.RandomAccessFileDelegator;
import com.snowalker.raft.core.store.support.SeekableFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 19:36
 * @desc RaftNodeStore文件实现
 *          格式：[currentTerm][votedFor_length][votedFor_content]
 *          思路：使用二进制方式存储term及votedFor
 *               文件格式（votedFor的实际值为节点id-->RaftNodeId的字符串形式）
 *               1. currentTerm          4字节
 *               2. votedFor内容长度      4字节        <------|
 *               3. votedFor内容         变长 具体长度由2决定- |
 */
public class FileRaftNodeStore implements RaftNodeStore {

	private static final String FILE_NAME = "node.bin";
	private static final long OFFSET_TERM = 0;
	private static final long OFFSET_VOTED_FOR = 4;

	// 初始化：length(term + votedFor) == 8
	private static final long TRUNCATE_SIZE = 8L;

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

	/**
	 * 从模拟文件读取，用于测试
	 * @param seekableFile
	 */
	public FileRaftNodeStore(SeekableFile seekableFile) {
		this.seekableFile = seekableFile;
		try {
			initializeOrLoadFile();
		} catch (IOException e) {
			throw new NodeStoreException(e);
		}
	}

	private void initializeOrLoadFile() throws IOException {

		if (seekableFile.size() == 0) {
			// 初始化：length(term + votedFor) == 8
			seekableFile.truncate(TRUNCATE_SIZE);     // 预分配长度
			seekableFile.seek(0);       // 定位到初始位置
			seekableFile.writeInt(0);         // 写入初始化term
			seekableFile.writeInt(0);         // 写入初始化votedFor
			return;
		}

		// 存在值则加载

		// 1.读取term
		currentTerm = seekableFile.readInt();
		// 2.读取votedFor
		int length = seekableFile.readInt();
		if (length > 0) {
			// votedFor大于0表明投过票
			byte[] bytes = new byte[length];
			seekableFile.read(bytes);
			votedFor = RaftNodeId.of(new String(bytes));
		}
	}

	/**
	 * 存储类别
	 *
	 * @return
	 */
	@Override
	public StoreType storeType() {
		return StoreType.FILE;
	}

	/**
	 * 获取currentTerm
	 */
	@Override
	public int getCurrentTerm() {
		return currentTerm;
	}

	/**
	 * 设置currentTerm
	 *
	 * @param currentTerm
	 */
	@Override
	public void setCurrentTerm(int currentTerm) {
		try {
			// 定位到term
			seekableFile.seek(OFFSET_TERM);
			seekableFile.writeInt(currentTerm);
		} catch (IOException e) {
			throw new NodeStoreException(e);
		}
		this.currentTerm = currentTerm;
	}

	/**
	 * 获取votedFor
	 */
	@Override
	public RaftNodeId getVotedFor() {
		return votedFor;
	}

	/**
	 * 设置votedFor
	 *
	 * @param votedFor
	 */
	@Override
	public void setVotedFor(RaftNodeId votedFor) {
		try {
			// 定位到votedFor开始位置
			seekableFile.seek(OFFSET_VOTED_FOR);

			if (votedFor == null) {
				// 初始化
				seekableFile.writeInt(0);
				seekableFile.truncate(TRUNCATE_SIZE);
			} else {
				byte[] bytes = votedFor.getVal().getBytes(StandardCharsets.UTF_8);
				seekableFile.writeInt(bytes.length);        // votedFor长度
				seekableFile.write(bytes);                  // votedFor内容
			}

		} catch (IOException e) {
			throw new NodeStoreException(e);
		}
		this.votedFor = votedFor;
	}

	/**
	 * 关闭文件
	 */
	@Override
	public void close() {
		try {
			seekableFile.close();
		} catch (IOException e) {
			throw new NodeStoreException(e);
		}
	}
}
