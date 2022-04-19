package com.snowalker.raft.core.store;

import com.snowalker.raft.core.leaderelection.node.RaftNodeId;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 12:30
 * @desc 角色持久化接口
 */
public interface RaftNodeStore {

	/**
	 * 存储类别
	 * @return
	 */
	StoreType storeType();

	/**获取currentTerm*/
	int getCurrentTerm();

	/**
	 * 设置currentTerm
	 * @param currentTerm
	 */
	void setCurrentTerm(int currentTerm);

	/**获取votedFor*/
	RaftNodeId getVotedFor();

	/**
	 * 设置votedFor
	 * @param votedFor
	 */
	void setVotedFor(RaftNodeId votedFor);

	/**
	 * 关闭文件
	 */
	void close();
}
