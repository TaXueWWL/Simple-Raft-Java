package com.snowalker.raft.core.leaderelection.role;

import com.snowalker.raft.core.leaderelection.task.ElectionTimeoutTimer;
import com.snowalker.raft.core.leaderelection.RoleType;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 22:52
 * @desc 候选人角色
 * 候选人candidate会有一个随机超时时间，睡眠随机时间之后开始发起leader选举
 */
@Getter
public class CandidateRole extends AbstractRaftNodeRole {

	/**
	 * 候选人得票数
	 */
	private int votedCount;
	/**
	 * 选举超时时间
	 */
	private ElectionTimeoutTimer electionTimeout;

	/**
	 * 构造方法 票数为1
	 * 场景：节点发起选举，变为candidate角色时使用
	 * @param term
	 * @param electionTimeout
	 */
	public CandidateRole(int term, ElectionTimeoutTimer electionTimeout) {
		this(term, 1, electionTimeout);
	}

	/**
	 * 构造方法  可指定票数
	 * 场景：收到其他节点投票时候使用
	 * @param term
	 * @param votedCount
	 * @param electionTimeout
	 */
	public CandidateRole(int term, int votedCount, ElectionTimeoutTimer electionTimeout) {
		super(RoleType.CANDIDATE, term);
		this.votedCount = votedCount;
		this.electionTimeout = electionTimeout;
	}

	/**
	 * 该接口允许主动增加票数
	 * 但是增加票数需要重置选举超时
	 * @param electionTimeout
	 * @return
	 */
	public CandidateRole incrVotesCount(ElectionTimeoutTimer electionTimeout) {
		// 重置选举超时
		this.electionTimeout.cancel();

		return new CandidateRole(this.term, votedCount + 1, electionTimeout);
	}

	/**
	 * 提供一个统一的接口，用于取消每个角色对应的当前选举超时或者定时任务
	 * 因为每个角色至多对应一个超时或者定时任务
	 * 在实际代码中，当从一个角色转换为另一个角色的时候，必须先取消当前角色的超时或者定时任务，
	 * 再创建新的超时或者定时任务
	 */
	@Override
	public void cancelTimeOutOrTaskOfCurrentRole() {
		// 对于candidate而言，取消超时
		electionTimeout.cancel();
	}

	@Override
	public String toString() {
		return "CandidateRole{" +
				"term=" + term +
				", votedCount=" + votedCount +
				", electionTimeout=" + electionTimeout +
				"}";
	}
}
