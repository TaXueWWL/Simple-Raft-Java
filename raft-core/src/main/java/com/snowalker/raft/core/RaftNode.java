package com.snowalker.raft.core;

import com.snowalker.raft.core.RaftNodeContext;
import com.snowalker.raft.core.leaderelection.RoleType;
import com.snowalker.raft.core.leaderelection.node.IRaftNode;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcRequest;
import com.snowalker.raft.core.leaderelection.role.AbstractRaftNodeRole;
import com.snowalker.raft.core.leaderelection.role.CandidateRole;
import com.snowalker.raft.core.leaderelection.role.FollowerRole;
import com.snowalker.raft.core.leaderelection.task.ElectionTimeoutTimer;
import com.snowalker.raft.core.store.RaftNodeStore;
import lombok.extern.slf4j.Slf4j;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/13 23:00
 * @desc raftNode实现
 */
@Slf4j
public class RaftNode implements IRaftNode {

	/**
	 * 核心组件上下文
	 */
	private final RaftNodeContext context;
	/**
	 * 是否启动标识
	 * 防止重复启动，并保证关闭只能在启动之后
	 */
	private boolean started;
	/**
	 * 当前节点的角色
	 */
	private AbstractRaftNodeRole role;

	public RaftNode(RaftNodeContext context) {
		this.context = context;
	}

	/**
	 * 启动
	 *      启动时，角色默认为follower，term为0
	 *      如果有日志的前提下，需要从最后一条日志计算最后的term
	 */
	@Override
	public synchronized void start() {

		// 已经启动过则不需要重复启动
		if (started) {
			return;
		}

		// 注册自身到eventBus
		context.eventBus().register(this);

		// 初始化连接器
		context.connector().initialize();

		// 获取存储
		RaftNodeStore store = context.store();

		// 角色：follower -> candidate
		changeToRole(new FollowerRole(store.getCurrentTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));

		started = true;

	}

	/**
	 * 角色变更
	 * @param newRole
	 */
	private void changeToRole(AbstractRaftNodeRole newRole) {
		log.info("node {} , role change to -> {}", context.currentId(), newRole);

		RaftNodeStore store = context.store();

		// 从存储中获取当前term
		store.setCurrentTerm(newRole.getTerm());

		// 从存储中读取并设置投票给谁
		if (newRole.getRoleType() == RoleType.FOLLOWER) {
			store.setVotedFor(((FollowerRole)newRole).getVotedFor());
		}

		// 变更角色
		role = newRole;
	}


	private ElectionTimeoutTimer scheduleElectionTimeout() {
		return this.context.scheduler().scheduleElectionTimeoutTimer(this::electionTimeout);
	}

	/**
	 * 设置选举超时需要做的事情为：变更节点角色及发送RequestVote消息到其他Node
	 */
	void electionTimeout() {
		context.taskExecutor().submit(this::doProcessElectionTimeout);
	}

	/**
	 * 执行选举超时角色变更
	 * 1. 先检查当前角色是否为Leader，如果是则打印告警并退出。
	 *      原因是对于leader而言，选举超时无意义，也不该发生
	 * 2. 对follower角色，执行业务操作，增加term，取消当前定时器，将角色从follower转变为candidate
	 *      并发送投票请求requestVote消息到其他节点
	 *
	 */
	private void doProcessElectionTimeout() {
		// leader角色不可能有选举超时
		if (role.getRoleType() == RoleType.LEADER) {
			log.warn("node {}, current node is Leader, should ignore election timeout.", context.currentId());
			return;
		}

		// 对于follower节点而言，变更角色为candidate后，发起选举
		// 对于candidate节点而言，再次发起选举
		// 选举term+1
		int newTerm = role.getTerm() + 1;
		role.cancelTimeOutOrTaskOfCurrentRole();

		log.info("[Leader-election] Begin current Leader election！");

		// 角色变更为candidate
		changeToRole(new CandidateRole(newTerm, scheduleElectionTimeout()));

		// 发送RequestVote消息
		RequestVoteRpcRequest requestVoteRpcRequest = RequestVoteRpcRequest.builder()
				.term(newTerm)
				.candidateId(context.currentId())
				.lastLogIndex(0)
				.lastLogTerm(0)
				.build();
		context.connector().sendRequestVote(requestVoteRpcRequest, context.group().listReplicationMembersWithoutSelf());
	}


	/**
	 * 关闭
	 *
	 * @throws InterruptedException
	 */
	@Override
	public synchronized void stop() throws InterruptedException {

		// 不允许在未启动时就关闭
		if (!started) {
			throw new IllegalStateException("node not started yet!");
		}

		// 关闭定时器
		context.scheduler().stop();

		// 关闭连接器
		context.connector().close();

		// 关闭任务执行线程池
		context.taskExecutor().shutdown();

		started = false;
	}
}
