package com.snowalker.raft.core.leaderelection.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:23
 * @desc 集群成员信息元数据
 *  包含：成员id 成员ip port
 *       当前成员的复制进度，主要用于日志复制
 */
public class RaftGroupMemberMetadata {

	@Getter
	private final RaftNodeEndPoint endPoint;
	@Getter
	private final RaftLogReplicaState replicaState;

	/**
	 * 带日志复制状态的构造方法
	 * @param raftNodeEndPoint
	 * @param raftLogReplicaState
	 */
	public RaftGroupMemberMetadata(RaftNodeEndPoint raftNodeEndPoint, RaftLogReplicaState raftLogReplicaState) {
		this.endPoint = raftNodeEndPoint;
		this.replicaState = raftLogReplicaState;
	}

	/**
	 * 不带日志复制状态的构造方法
	 * @param endPoint
	 */
	public RaftGroupMemberMetadata(RaftNodeEndPoint endPoint) {
		this(endPoint, null);
	}

	RaftLogReplicaState checkLogReplicaState() {
		if (replicaState == null) {
			throw new IllegalStateException("RaftLogReplicaState Not Set!");
		}
		return replicaState;
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


}
