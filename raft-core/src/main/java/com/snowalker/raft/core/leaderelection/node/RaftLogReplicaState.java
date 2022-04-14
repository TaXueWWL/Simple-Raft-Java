package com.snowalker.raft.core.leaderelection.node;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:25
 * @desc TODO 日志复制进度
 */
public class RaftLogReplicaState {

	private boolean replicating;


	public int nextLogIndex() {
		return -1;
	}

	public int matchedLogIndex() {
		return -1;
	}

	public boolean advance(int lastEntryIndex) {
		return false;
	}

	public boolean isReplicating() {
		return false;
	}

	public void setReplicating(boolean b) {
	}

	public long getLastReplicatedAt() {
		return -1;
	}

	public void setLastReplicatedAt(long replicatedAt) {
	}

	public boolean backOffNextIndex() {
		return false;
	}
}
