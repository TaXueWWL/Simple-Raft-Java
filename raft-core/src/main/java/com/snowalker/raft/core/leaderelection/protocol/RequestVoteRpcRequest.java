package com.snowalker.raft.core.leaderelection.protocol;

import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 11:36
 * @desc 请求投票RPC 请求实体
 */
@Data
public class RequestVoteRpcRequest {

	/**当前选举的任期*/
	private int term;

	/**候选人集群节点id，一般是发送者自己*/
	private RaftNodeId candidateId;

	/**候选人最后一条日志索引*/
	private int lastLogIndex = 0;

	/**候选人最后一条日志任期*/
	private int lastLogTerm = 0;
}
