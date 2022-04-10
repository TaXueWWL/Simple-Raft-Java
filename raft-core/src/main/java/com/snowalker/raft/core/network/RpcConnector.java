package com.snowalker.raft.core.network;

import com.snowalker.raft.core.leaderelection.node.RaftNodeEndPoint;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesRpcRequest;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesRpcResponse;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcRequest;
import com.snowalker.raft.core.leaderelection.protocol.RequestVoteRpcResponse;

import java.util.Collection;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 12:26
 * @desc RPC调用抽象
 */
public interface RpcConnector {

	/**RPC框架类型*/
	void frameworkType();

	/**初始化*/
	void initialize();

	/**
	 * 发送RequestVote消息到其他节点（群发）
	 * @param voteRpc
	 * @param targetEndpoints
	 */
	void sendRequestVote(RequestVoteRpcRequest voteRpc, Collection<RaftNodeEndPoint> targetEndpoints);

	/**
	 * 响应RequestVote消息给某节点（一般都是响应给了发起投票的候选人节点）
	 * @param resp
	 * @param targetEndpoint
	 */
	void replyRequestVote(RequestVoteRpcResponse resp, RaftNodeEndPoint targetEndpoint);

	/**
	 * 发送AppendEntries消息到某个raft节点（点对点发送，原因在于每个raft节点同步日志进度是独立的）
	 * @param appendEntriesRpc
	 * @param targetEndpoint
	 */
	void sendAppendEntries(AppendEntriesRpcRequest appendEntriesRpc, RaftNodeEndPoint targetEndpoint);

	/**
	 * 响应AppendEntries结果到某个raft节点
	 * @param resp
	 * @param targetEndpoint
	 */
	void replyAppendEntries(AppendEntriesRpcResponse resp, RaftNodeEndPoint targetEndpoint);

	/**
	 * 关闭RPC连接器
	 */
	void close();
}
