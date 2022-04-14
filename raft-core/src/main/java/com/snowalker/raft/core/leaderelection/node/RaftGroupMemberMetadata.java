package com.snowalker.raft.core.leaderelection.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:23
 * @desc 集群成员信息元数据
 *  包含：成员id 成员ip port
 *       当前成员的复制进度，主要用于日志复制
 */
@Data
public class RaftGroupMemberMetadata {

	private final RaftNodeEndPoint endPoint;
	private final RaftLogReplicaState replicaState;
	private boolean major;
	private boolean removing = false;

	/**
	 * 带日志复制状态的构造方法
	 * @param raftNodeEndPoint
	 * @param raftLogReplicaState
	 */
	public RaftGroupMemberMetadata(RaftNodeEndPoint raftNodeEndPoint,
	                               RaftLogReplicaState raftLogReplicaState,
	                               boolean major) {
		this.endPoint = raftNodeEndPoint;
		this.replicaState = raftLogReplicaState;
		this.major = major;
	}

	/**
	 * 不带日志复制状态的构造方法
	 * @param endPoint
	 */
	public RaftGroupMemberMetadata(RaftNodeEndPoint endPoint) {
		this(endPoint, null, true);
	}

	RaftLogReplicaState checkLogReplicaState() {
		if (replicaState == null) {
			throw new IllegalStateException("RaftLogReplicaState Not Set!");
		}
		return replicaState;
	}

	boolean idEquals(RaftNodeId id) {
		return endPoint.getId().equals(id);
	}

	boolean isReplicationStateSet() {
		return replicaState != null;
	}

	/**
	 * 获取下一个日志索引
	 * @return
	 */
	public int getNextLogIndex() {
		return checkLogReplicaState().nextLogIndex();
	}

	/**
	 * 获取匹配日志索引
	 * @return
	 */
	public int getMatchedLogIndex() {
		return checkLogReplicaState().matchedLogIndex();
	}

	boolean advanceReplicatingState(int lastEntryIndex) {
		return checkLogReplicaState().advance(lastEntryIndex);
	}

	boolean backOffNextIndex() {
		return checkLogReplicaState().backOffNextIndex();
	}

	void replicateNow() {
		replicateAt(System.currentTimeMillis());
	}

	void replicateAt(long replicatedAt) {
		RaftLogReplicaState replicatingState = checkLogReplicaState();
		replicatingState.setReplicating(true);
		replicatingState.setLastReplicatedAt(replicatedAt);
	}

	boolean isReplicating() {
		return checkLogReplicaState().isReplicating();
	}

	void stopReplicating() {
		checkLogReplicaState().setReplicating(false);
	}

	boolean shouldReplicate(long readTimeout) {
		RaftLogReplicaState replicatingState = checkLogReplicaState();
		return !replicatingState.isReplicating() ||
				System.currentTimeMillis() - replicatingState.getLastReplicatedAt() >= readTimeout;
	}

	public boolean isMajor() {
		return major;
	}

}
