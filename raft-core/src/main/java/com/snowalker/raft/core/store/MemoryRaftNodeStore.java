package com.snowalker.raft.core.store;

import com.snowalker.raft.common.annotation.TestOnly;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 19:25
 * @desc 角色状态持久化 内存实现，用于测试
 */
@TestOnly
public class MemoryRaftNodeStore implements RaftNodeStore {

	private int currentTerm;

	private RaftNodeId votedFor;

	public MemoryRaftNodeStore() {
		this(0, null);
	}

	public MemoryRaftNodeStore(int currentTerm, RaftNodeId votedFor) {
		this.currentTerm = currentTerm;
		this.votedFor = votedFor;
	}

	/**
	 * 存储类别
	 *
	 * @return
	 */
	@Override
	public StoreType storeType() {
		return StoreType.MEMORY;
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
		this.votedFor = votedFor;
	}

	/**
	 * 关闭文件
	 */
	@Override
	public void close() {
	}
}
