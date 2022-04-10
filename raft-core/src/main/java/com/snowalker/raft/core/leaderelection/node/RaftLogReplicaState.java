package com.snowalker.raft.core.leaderelection.node;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:25
 * @desc TODO 日志复制进度
 */
public class RaftLogReplicaState {

	public int nextLogIndex() {
		return -1;
	}

	public int matchedLogIndex() {
		return -1;
	}
}
