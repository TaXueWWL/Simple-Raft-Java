package com.snowalker.raft.core.leaderelection.role;

import com.snowalker.raft.core.leaderelection.RoleType;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 12:41
 * @desc 抽象raft角色
 * Raft中一共有三个角色：Leader Follower Candidate
 * 在不同条件下会转变为不同的角色，
 * AbstractRaftNodeRole为不同角色的通用抽象父类，抽取一些通用的属性
 * 对于Leader Follower Candidate 而言，共同具备的属性就是 term
 */
public abstract class AbstractRaftNodeRole {

	/**
	 * 角色类别 Leader / Follower / Candidate
	 */
	@Getter
	private final RoleType roleType;
	/**
	 * 选举任期
	 */
	@Getter
	protected final int term;


	public AbstractRaftNodeRole(RoleType roleType, int term) {
		this.roleType = roleType;
		this.term = term;
	}

	/**
	 * 提供一个统一的接口，用于取消每个角色对应的当前选举超时或者定时任务
	 * 因为每个角色至多对应一个超时或者定时任务
	 * 在实际代码中，当从一个角色转换为另一个角色的时候，必须先取消当前角色的超时或者定时任务，
	 * 再创建新的超时或者定时任务
	 */
	public abstract void cancelTimeOutOrTaskOfCurrentRole();

	public abstract RaftNodeId getLeaderId(RaftNodeId selfId);

}
