package com.snowalker.raft.core.leaderelection.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 12:08
 * @desc 日志同步RPC 响应实体
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class AppendEntriesRpcResponse {

	/**选举term*/
	private int term;

	/**日志是否追加成功*/
	private boolean success;

	public static AppendEntriesRpcResponse of(int term, boolean success) {
		return new AppendEntriesRpcResponse(term, success);
	}
}
