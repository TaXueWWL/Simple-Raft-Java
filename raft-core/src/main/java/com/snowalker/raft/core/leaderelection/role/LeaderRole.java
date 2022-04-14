package com.snowalker.raft.core.leaderelection.role;

import com.snowalker.raft.core.leaderelection.RoleType;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.task.LogReplicationTask;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 22:48
 * @desc 领导者角色
 */
public class LeaderRole extends AbstractRaftNodeRole {

	/**
	 * 日志复制定时器
	 * 一旦角色成为领导者，则需要为其他的follower发送心跳，同步日志
	 */
	private final LogReplicationTask logReplicationTask;

	public LeaderRole(int term, LogReplicationTask logReplicationTask) {
		super(RoleType.LEADER, term);
		this.logReplicationTask = logReplicationTask;
	}

	/**
	 * 提供一个统一的接口，用于取消每个角色对应的当前选举超时或者定时任务
	 * 因为每个角色至多对应一个超时或者定时任务
	 * 在实际代码中，当从一个角色转换为另一个角色的时候，必须先取消当前角色的超时或者定时任务，
	 * 再创建新的超时或者定时任务
	 */
	@Override
	public void cancelTimeOutOrTaskOfCurrentRole() {
		// 对leader而言，需要取消日志复制任务
		logReplicationTask.cancel();
	}

	@Override
	public RaftNodeId getLeaderId(RaftNodeId selfId) {
		return selfId;
	}

	@Override
	public String toString() {
		return "LeaderRole{" +
				"term=" + term +
				", logReplicationTask=" + logReplicationTask +
				"}";
	}
}
