package com.snowalker.raft.core.leaderelection.role;

import com.snowalker.raft.core.leaderelection.task.ElectionTimeoutTimer;
import com.snowalker.raft.core.leaderelection.RoleType;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import lombok.Getter;
import lombok.Setter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 22:47
 * @desc 跟随者角色
 * 对于跟随者角色：需要关注投票过的节点
 * 需要记录当前任期内的leader的id
 * follower超时则会转变为候选人candidate
 */
@Getter
@Setter
public class FollowerRole extends AbstractRaftNodeRole {

	/**
	 * follower投过票的节点，可能为空
	 */
	private RaftNodeId votedFor;
	/**
	 * 当前term的leader节点Id，可能为空
	 */
	private RaftNodeId leaderId;
	/**
	 * 选举超时
	 */
	private ElectionTimeoutTimer electionTimeout;

	public FollowerRole(int term, RaftNodeId votedFor, RaftNodeId leaderId, ElectionTimeoutTimer electionTimeout) {
		super(RoleType.FOLLOWER, term);
		this.votedFor = votedFor;
		this.leaderId = leaderId;
		this.electionTimeout = electionTimeout;
	}

	public FollowerRole(RoleType roleType, int term) {
		super(roleType, term);
	}

	/**
	 * 提供一个统一的接口，用于取消每个角色对应的当前选举超时或者定时任务
	 * 因为每个角色至多对应一个超时或者定时任务
	 * 在实际代码中，当从一个角色转换为另一个角色的时候，必须先取消当前角色的超时或者定时任务，
	 * 再创建新的超时或者定时任务
	 */
	@Override
	public void cancelTimeOutOrTaskOfCurrentRole() {
		// 对于follower而言，取消超时
		electionTimeout.cancel();
	}

	@Override
	public RaftNodeId getLeaderId(RaftNodeId selfId) {
		return leaderId;
	}

	@Override
	public String toString() {
		return "FollowerRole{" +
				"term=" + term +
				", votedFor=" + votedFor +
				", leaderId=" + leaderId +
				", electionTimeout=" + electionTimeout +
				"}";
	}
}
