package com.snowalker.raft.core.leaderelection;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.snowalker.raft.core.leaderelection.node.IRaftNode;
import com.snowalker.raft.core.leaderelection.node.RaftGroupMemberMetadata;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.protocol.*;
import com.snowalker.raft.core.leaderelection.role.AbstractRaftNodeRole;
import com.snowalker.raft.core.leaderelection.role.CandidateRole;
import com.snowalker.raft.core.leaderelection.role.FollowerRole;
import com.snowalker.raft.core.leaderelection.role.LeaderRole;
import com.snowalker.raft.core.leaderelection.task.ElectionTimeoutTimer;
import com.snowalker.raft.core.leaderelection.task.LogReplicationTask;
import com.snowalker.raft.core.store.RaftNodeStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

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
	@Getter
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

		// 角色：follower -> candidate (投票给自己 scheduleElectionTimeout)
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

		// 发送RequestVote消息 到 集群中其他机器
		RequestVoteRpcRequest requestVoteRpcRequest = RequestVoteRpcRequest.builder()
				.term(newTerm)
				.candidateId(context.currentId())
				.lastLogIndex(0)
				.lastLogTerm(0)
				.build();
		context.connector().sendRequestVote(requestVoteRpcRequest, context.group().listReplicationMembersWithoutSelf());
	}

	/**
	 * 接受到投票请求的处理逻辑
	 * @param rpcMessage
	 */
	@Subscribe
	public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
		context.taskExecutor().submit(
				() -> context.connector().replyRequestVote(
						doProcessRequestVoteRpc(rpcMessage),    // 处理投票请求并返回响应
						context.findMember(rpcMessage.getSourceNodeId()).getEndPoint()
				)
		);
	}

	/**
	 * 接收到投票响应的处理逻辑
	 * @param result
	 */
	@Subscribe
	private void onReceiveRequestVoteResponse(RequestVoteRpcResponse result) {
		context.taskExecutor().submit(() -> doProcessRequestVoteResponse(result));
	}

	/**
	 * 其他节点接收到来自leader的心跳消息
	 */
	@Subscribe
	public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
		context.taskExecutor().submit(
				() -> context.connector().replyAppendEntries(
						doProcessAppendEntriesRpc(rpcMessage),
						// 发送消息的节点
						context.findMember(rpcMessage.getSourceNodeId()).getEndPoint()
				)
		);
	}

	/**
	 * Leader节点接收到其他节点的心跳响应
	 * @param resultMessage
	 */
	@Subscribe
	public void onReceiveAppendEntriesRpcResponse(AppendEntriesResultMessage resultMessage) {
		context.taskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
	}

	/**
	 * Leader处理日志追加响应
	 * @param resultMessage
	 */
	private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {

		AppendEntriesRpcResponse response = resultMessage.get();

		// 如果对方的term比自己的大，则自己退化为follower
		if (response.getTerm() > role.getTerm()) {
			becomeFollower(response.getTerm(), null, null, true);
			return;
		}

		// 检查自己的角色, 如果不是leader但收到了日志追加响应，则忽略
		if (role.getRoleType() != RoleType.LEADER) {
			log.warn("Receive append entries result from node :{}, but current node is not leader, so ignore.", resultMessage.getSourceNodeId());
		}
		return;
	}

	/**
	 * 执行追加日志entries逻辑
	 * @param rpcMessage
	 */
	private AppendEntriesRpcResponse doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
		AppendEntriesRpcRequest rpc = rpcMessage.getRpc();

		// (case 1) 如果对方的term比自己的小  返回自己的term
		if (rpc.getTerm() < role.getTerm()) {
			return AppendEntriesRpcResponse.of(role.getTerm(), false);
		}

		//(case 2) 如果对方的term比自己大，则退化为follower
		if (rpc.getTerm() > role.getTerm()) {
			becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
			// 追加日志
			return AppendEntriesRpcResponse.of(rpc.getTerm(), appendLogEntries(rpc));
		}

		Preconditions.checkArgument(rpc.getTerm() == role.getTerm(), "rpc.getTerm() should == role.getTerm()");

		switch (role.getRoleType()) {

			case FOLLOWER:

				// (case 3) 设置leaderId并重置选举定时器
				becomeFollower(role.getTerm(), ((FollowerRole)role).getVotedFor(), rpc.getLeaderId(), true);
				// 追加日志
				return AppendEntriesRpcResponse.of(rpc.getTerm(), appendLogEntries(rpc));

			case CANDIDATE:

				// (case 4) 如果有两个candidate角色，并且另外一个candidate先成为了leader。
				// 则当前节点退化为follower并重置选举定时器
				becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
				// 追加日志
				return AppendEntriesRpcResponse.of(rpc.getTerm(), appendLogEntries(rpc));

			case LEADER:

				// (case 5) Leader收到AppendEntries消息，打印告警日志
				log.warn("[WRONG APPEND_ENTRIES] Role: [Leader] Received append entries rpc from another LEADER {}", rpc.getLeaderId());
				return AppendEntriesRpcResponse.of(rpc.getTerm(), false);

			default:

				throw new IllegalStateException("UnExpected node role [" + role.getRoleType() + "]");
		}
	}

	// TODO
	private boolean appendLogEntries(AppendEntriesRpcRequest rpc) {
		return true;
	}

	private RequestVoteRpcResponse doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {

		// 如果对方term比自己小，不投票并返回自己的term
		RequestVoteRpcRequest rpc = rpcMessage.getRpc();
		if (rpc.getTerm() < role.getTerm()) {
			log.debug("term from RequestVoteRpcMessage < currentTerm, don't vote. ({} < {})", rpc.getTerm(), role.getTerm());
			// 返回当前Node的term，投票失败
			return RequestVoteRpcResponse.of(role.getTerm(), false);
		}

		// 处理投票逻辑
		boolean voteForCandidate = true;

		// 如果对方的term比自己的大，则切换当前节点为follower角色
		// 重新创建选举超时
		if (rpc.getTerm() > role.getTerm()) {
			becomeFollower(rpc.getTerm(), (voteForCandidate ? rpc.getCandidateId() : null), null, true);
			return RequestVoteRpcResponse.of(rpc.getTerm(), voteForCandidate);
		}

		// 本地的term与投票消息中的term一致
		switch (role.getRoleType()) {
			case FOLLOWER:
				FollowerRole follower = (FollowerRole) role;
				RaftNodeId votedFor = follower.getVotedFor();

				// 进行投票
				// 1. 自己还没投过票，并且对方日志比自己的新
				// 2. 自己已经给对方投过票
				if ((votedFor == null && voteForCandidate) || Objects.equals(votedFor, rpc.getCandidateId())) {
					becomeFollower(role.getTerm(), rpc.getCandidateId(), null, true);
					return RequestVoteRpcResponse.of(rpc.getTerm(), true);
				}
				return RequestVoteRpcResponse.of(role.getTerm(), false);

			case CANDIDATE: // 候选人已经给自己投票了 所以不会给别的节点投票
			case LEADER:
				return RequestVoteRpcResponse.of(role.getTerm(), false);
			default:
				throw new IllegalStateException("Unexpected node role [" + role.getRoleType() + "]");

		}
	}

	/**
	 * 角色变更方法，当前candidate变更为follower，原因是当前节点的term小于接收到的投票请求的term
	 * @param term
	 * @param votedFor
	 * @param leaderId
	 * @param scheduleElectionTimeout  是否设置选举超时
	 */
	private void becomeFollower(int term,
	                            RaftNodeId votedFor,
	                            RaftNodeId leaderId,
	                            boolean scheduleElectionTimeout) {
		role.cancelTimeOutOrTaskOfCurrentRole();    // 取消超时或者定时器
		if (leaderId != null && !leaderId.equals(role.getLeaderId(context.currentId()))) {
			log.info("current leader is {}, term {}.", leaderId, term);
		}
		// 重新创建选举超时定时器或者空定时器
		ElectionTimeoutTimer electionTimeoutTimer = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeoutTimer.NONE;
		changeToRole(new FollowerRole(term, votedFor, leaderId, electionTimeoutTimer));
	}


	private void doProcessRequestVoteResponse(RequestVoteRpcResponse result) {
		// 如果对象的term比自己的大，则退化为follower (已经有更高term的candidate当选)
		if (result.getTerm() > role.getTerm()) {
			becomeFollower(result.getTerm(), null, null, true);
			return;
		}
		// 如果自己不是candidate角色，则忽略--> 只有candidate才会处理响应
		if (role.getRoleType() != RoleType.CANDIDATE) {
			log.debug("receive request vote result and current role is not candidate, ignore. Current role is [{}]", role.getRoleType());
			return;
		}
		// 如果对方term比自己的小或者对象没有给自己投票，则忽略
		if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
			log.debug("result.getTerm < currentTerm or result.isVoteGranted == false, result:[{}]", result);
			return;
		}

		// 获取当前票数 + 1 --> 投票成功
		int currentVotesCount = ((CandidateRole)role).getVotedCount() + 1;
		// 节点数量
		int countOfMajor = context.group().getCountOfMajor();
		log.debug("Votes count {}, node count {}.", currentVotesCount, countOfMajor);

		// 取消选举超时定时器
		role.cancelTimeOutOrTaskOfCurrentRole();

		// 票数过半
		if (currentVotesCount > (countOfMajor / 2)) {
			// 当前节点成为leader
			log.info("[Role-Change-To-Leader] Current Node become Leader, term:{}.", role.getTerm());

			// resetReplicatingStates();
			// 进行log复制
			changeToRole(new LeaderRole(role.getTerm(), scheduleLogReplicationTask()));

			// 成为leader之后打印日志，设置自己为leader，启动日志复制定时器，并添加一条NO-OP日志，标记成为leader
			// context.log().appendEntry(role.getTerm());
		} else {
			// 票数未过半，无法当选leader 修改收到的投票数，并重新创建选举超时定时器
			changeToRole(new CandidateRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
		}
	}

	private LogReplicationTask scheduleLogReplicationTask() {
		return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
	}

	/**
	 *  日志复制
	 */
	private void replicateLog() {
		context.taskExecutor().submit(this::doReplicateLog);
	}

	/**
	 * TODO 日志复制实现
	 * 为集群中非leader节点发送appendEntries消息
	 */
	private void doReplicateLog() {
		log.debug("Begin doReplicateLog.");
		for (RaftGroupMemberMetadata member : context.group().listReplicationTarget()) {
			doMemberReplicateLog(member);
		}
	}

	private void doMemberReplicateLog(RaftGroupMemberMetadata member) {
		AppendEntriesRpcRequest rpc = AppendEntriesRpcRequest.builder()
				.term(role.getTerm())
				.leaderId(context.currentId())
				.prevLogIndex(0)
				.prevLogTerm(0)
				.leaderCommit(0).build();
		context.connector().sendAppendEntries(rpc, member.getEndPoint());
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
