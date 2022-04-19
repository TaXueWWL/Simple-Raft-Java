package com.snowalker.raft.core.leaderelection.protocol;

import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.network.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/13 23:54
 * @desc 请求投票实际RPC Message
 */
@AllArgsConstructor
@Data
public class AppendEntriesRpcMessage {

	private AppendEntriesRpcRequest rpc;

	/**
	 * 发送心跳的源node
	 */
	private RaftNodeId sourceNodeId;

	private Channel channel;
}
