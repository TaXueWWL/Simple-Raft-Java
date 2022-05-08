package com.snowalker.raft.core.leaderelection.protocol;

import com.google.common.collect.Lists;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.log.LogEntry;
import lombok.*;

import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 11:37
 * @desc 日志同步RPC 请求实体
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppendEntriesRpcRequest {

	/**当前任期*/
	private int term;

	/**Leader节点的id，Follower会进行记录*/
	private RaftNodeId leaderId;

	/**前一条日志的索引*/
	private int prevLogIndex = 0;

	/**前一条日志的term*/
	private int prevLogTerm;

	/**本次请求复制的日志条目集合*/
	private List<LogEntry> entries = Lists.newArrayList();

	/**Leader的commitIndex*/
	private int leaderCommit;
}
