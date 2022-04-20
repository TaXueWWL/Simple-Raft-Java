package com.snowalker.raft.core.leaderelection;

import com.google.common.collect.Lists;
import com.snowalker.raft.common.annotation.DisplayName;
import com.snowalker.raft.core.leaderelection.RaftNode;
import com.snowalker.raft.core.leaderelection.RaftNodeBuilder;
import com.snowalker.raft.core.leaderelection.node.RaftNodeEndPoint;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcMessage;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcRequest;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcResponse;
import com.snowalker.raft.core.leaderelection.role.CandidateRole;
import com.snowalker.raft.core.leaderelection.role.FollowerRole;
import com.snowalker.raft.core.leaderelection.role.LeaderRole;
import com.snowalker.raft.core.scheduler.DirectTaskExecutor;
import com.snowalker.raft.core.store.StoreType;
import com.snowalker.raft.mock.MockConnector;
import com.snowalker.raft.mock.NullScheduler;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/19 22:42
 * @desc
 */
public class RaftNodeTestCase {

	private RaftNodeBuilder newNodeBuilder(RaftNodeId selfId, RaftNodeEndPoint... endPoints) {
		return new RaftNodeBuilder(Lists.newArrayList(endPoints), selfId)
				.setScheduler(new NullScheduler())
				.setConnector(new MockConnector())
				.setTaskExecutor(new DirectTaskExecutor())
				.setStoreType(StoreType.MEMORY);
	}

	@DisplayName("系统启动测试")
	@Test
	public void testStart() {
		RaftNode node = (RaftNode) newNodeBuilder(
				RaftNodeId.of("A"),
				new RaftNodeEndPoint("A", "localhost", 8080)).build();
		node.start();

		FollowerRole role = (FollowerRole) node.getRole();

		Assert.assertEquals(0, role.getTerm());
		Assert.assertNull(role.getVotedFor());
	}

	@DisplayName("Follower角色的选举超时-转变为candidate角色")
	@Test
	public void testFollowerElectionTimeout() {
		RaftNode node = (RaftNode) newNodeBuilder(
				RaftNodeId.of("A"),
				new RaftNodeEndPoint("A", "localhost", 8080),
				new RaftNodeEndPoint("B", "localhost", 8081),
				new RaftNodeEndPoint("C", "localhost", 8082)
		).build();

		node.start();
		node.electionTimeout();

		// 选举开始之后，将初始term值设置为1
		CandidateRole candidateRole = (CandidateRole) node.getRole();

		Assert.assertEquals(1, candidateRole.getTerm());
		Assert.assertEquals(1, candidateRole.getVotedCount());

		// 获取mock RpcConnector
		MockConnector mockConnector = (MockConnector) node.getContext().connector();
		// 发送RequestVote到集群中其他节点
		RequestVoteRpcRequest rpc = (RequestVoteRpcRequest) mockConnector.getRPC();

		Assert.assertEquals(1, rpc.getTerm());
		Assert.assertEquals(RaftNodeId.of("A"), rpc.getCandidateId());
		Assert.assertEquals(0, rpc.getLastLogIndex());
		Assert.assertEquals(0, rpc.getLastLogTerm());
	}

	@DisplayName("收到RequestVote消息：Follower节点收到其他节点RequestVote消息，投票并设置自己的votedFor为消息来源节点的id")
	@Test
	public void testOnReceiveRequestVoteRpcFollower() {
		RaftNode node = (RaftNode) newNodeBuilder(
				RaftNodeId.of("A"),
				new RaftNodeEndPoint("A", "localhost", 8080),
				new RaftNodeEndPoint("B", "localhost", 8081),
				new RaftNodeEndPoint("C", "localhost", 8082)
		).build();

		node.start();

		RequestVoteRpcRequest rpc = RequestVoteRpcRequest.of(1, RaftNodeId.of("C"), 0, 0);

		node.onReceiveRequestVoteRpc(new RequestVoteRpcMessage(rpc, RaftNodeId.of("C"), null));

		// 获取mock RpcConnector
		MockConnector mockConnector = (MockConnector) node.getContext().connector();

		RequestVoteRpcResponse response = (RequestVoteRpcResponse) mockConnector.getResult();

		Assert.assertEquals(1, response.getTerm());
		Assert.assertTrue(response.isVoteGranted());
		Assert.assertEquals(RaftNodeId.of("C"), ((FollowerRole)node.getRole()).getVotedFor());
	}

	@DisplayName("收到RequestVote响应测试")
	@Test
	public void testOnReceiveRequestVoteResult() {
		RaftNode node = (RaftNode) newNodeBuilder(
				RaftNodeId.of("A"),
				new RaftNodeEndPoint("A", "localhost", 8080),
				new RaftNodeEndPoint("B", "localhost", 8081),
				new RaftNodeEndPoint("C", "localhost", 8082)
		).build();

		node.start();

		// 必须显式调用选举超时，转变为candidate角色
		node.electionTimeout();

		node.onReceiveRequestVoteResponse(RequestVoteRpcResponse.of(1, true));

		LeaderRole role = (LeaderRole) node.getRole();

		Assert.assertEquals(1, role.getTerm());
	}

	@DisplayName("成为Leader节点之后的心跳消息")
	@Test
	public void testReplicateLog() {
		RaftNode node = (RaftNode) newNodeBuilder(
				RaftNodeId.of("A"),
				new RaftNodeEndPoint("A", "localhost", 8080),
				new RaftNodeEndPoint("B", "localhost", 8081),
				new RaftNodeEndPoint("C", "localhost", 8082)
		).build();

		node.start();

		// 发送RequestVote消息
		node.electionTimeout();

		node.onReceiveRequestVoteResponse(RequestVoteRpcResponse.of(1, true));

		// 模拟发送两条复制日志消息
		node.replicateLog();

		MockConnector mockConnector = (MockConnector) node.getContext().connector();

		// 期望三条日志
		Assert.assertEquals(3, mockConnector.getMessageCount());

		// 检查目标节点
		List<MockConnector.Message> messages = mockConnector.getMessages();
		List<RaftNodeId> destinationNodeIds = messages.subList(1, 3).stream()
				.map(MockConnector.Message::getDestinationNodeId)
				.collect(Collectors.toList());

		Assert.assertEquals(2, destinationNodeIds.size());
		Assert.assertTrue(destinationNodeIds.contains(RaftNodeId.of("B")));
		Assert.assertTrue(destinationNodeIds.contains(RaftNodeId.of("C")));

	}
}
