package com.snowalker.raft.mock;

import com.google.common.collect.Lists;
import com.snowalker.raft.core.leaderelection.RaftNode;
import com.snowalker.raft.core.leaderelection.node.RaftNodeEndPoint;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesRpcRequest;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesRpcResponse;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcRequest;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcResponse;
import com.snowalker.raft.core.network.RpcConnector;
import com.snowalker.raft.core.network.RpcFrameworkType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/19 15:20
 * @desc 测试用RPC组件
 * 主要作用就是将RPC消息暂存在某个地方，实际上不进行发送
 */
public class MockConnector implements RpcConnector {

	/**存放消息的链表*/
	private LinkedList<Message> messages = Lists.newLinkedList();

	/**
	 * RPC框架类型
	 * @return
	 */
	@Override
	public RpcFrameworkType frameworkType() {

		return RpcFrameworkType.DEFAULT;
	}

	/**
	 * 初始化
	 */
	@Override
	public void initialize() {

	}

	/**
	 * 发送RequestVote消息到其他节点（群发）
	 *
	 * @param voteRpc
	 * @param targetEndpoints
	 */
	@Override
	public void sendRequestVote(RequestVoteRpcRequest voteRpc, Collection<RaftNodeEndPoint> targetEndpoints) {
		// 对于多目标节点，这里没有完全处理
		Message m = Message.builder().build();
		m.rpc = voteRpc;
		messages.add(m);
	}

	/**
	 * 响应RequestVote消息给某节点（一般都是响应给了发起投票的候选人节点）
	 *
	 * @param resp
	 * @param targetEndpoint
	 */
	@Override
	public void replyRequestVote(RequestVoteRpcResponse resp, RaftNodeEndPoint targetEndpoint) {
		Message m = Message.builder().build();
		m.result = resp;
		m.destinationNodeId = targetEndpoint.getId();
		messages.add(m);
	}

	/**
	 * 发送AppendEntries消息到某个raft节点（点对点发送，原因在于每个raft节点同步日志进度是独立的）
	 *
	 * @param appendEntriesRpc
	 * @param targetEndpoint
	 */
	@Override
	public void sendAppendEntries(AppendEntriesRpcRequest appendEntriesRpc, RaftNodeEndPoint targetEndpoint) {
		Message m = Message.builder().build();
		m.rpc = appendEntriesRpc;
		m.destinationNodeId = targetEndpoint.getId();
		messages.add(m);
	}

	/**
	 * 响应AppendEntries结果到某个raft节点
	 *
	 * @param resp
	 * @param targetEndpoint
	 */
	@Override
	public void replyAppendEntries(AppendEntriesRpcResponse resp, RaftNodeEndPoint targetEndpoint) {
		Message m = Message.builder().build();
		m.result = resp;
		m.destinationNodeId = targetEndpoint.getId();
		messages.add(m);
	}

	/**
	 * 关闭RPC连接器
	 */
	@Override
	public void close() {

	}


	////////////////////////////////////////RPC消息辅助方法 方便校验RPC消息发送结果////////////////////////////////////////////

	/**
	 * 获取最后一条消息
	 * @return
	 */
	public Message getLastMessage() {
		return messages.isEmpty() ? null : messages.getLast();
	}

	/**
	 * 获取最后一条消息或默认
	 * @return
	 */
	public Message getLastMsgOrDefault() {
		return messages.isEmpty() ? Message.builder().build() : messages.getLast();
	}

	/**
	 * 获取最后一条RPC消息
	 * @return
	 */
	public Object getRPC() {
		return getLastMsgOrDefault().getRpc();
	}

	/**
	 * 获取最后一条Result消息
	 * @return
	 */
	public Object getResult() {
		return getLastMsgOrDefault().getResult();
	}

	/**
	 * 获取最后一条消息的目标节点
	 * @return
	 */
	public RaftNodeId getDestinationNodeId() {
		return getLastMsgOrDefault().getDestinationNodeId();
	}

	/**
	 * 获取消息数量
	 * @return
	 */
	public int getMessageCount() {
		return messages.size();
	}

	/**
	 * 获取所有消息
	 * @return
	 */
	public List<Message> getMessages() {
		return Lists.newArrayList(messages);
	}

	/**
	 * 清空消息
	 */
	public void clearMessage() {
		messages.clear();
	}

	////////////////////////////////////////RPC消息辅助方法结束////////////////////////////////////////////


	@Getter
	@ToString
	@Builder
	public static class Message {
		private Object rpc;                         // RPC消息
		private RaftNodeId destinationNodeId;       // 目标raft节点
		private Object result;                      // 结果
	}
}
