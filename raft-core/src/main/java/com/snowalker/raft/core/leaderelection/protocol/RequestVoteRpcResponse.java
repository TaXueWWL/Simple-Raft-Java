package com.snowalker.raft.core.leaderelection.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 11:42
 * @desc 投票请求响应实体
 * 一个简单的Raft论文的Java实现
 */
@AllArgsConstructor
@Getter
@ToString
public class RequestVoteRpcResponse {

	/**选举任期*/
	private final int term;

	/**是否进行了投票*/
	private final boolean voteGranted;
}
