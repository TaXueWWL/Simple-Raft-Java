package com.snowalker.raft.core.log;

import lombok.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/26 23:29
 * @desc 日志元信息
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogEntryMeta {

	/**日志类型*/
	private int kind;

	/**任期*/
	private int term;

	/**索引*/
	private int index;

	public static LogEntryMeta of(int kind, int term, int index) {
		return LogEntryMeta.builder().kind(kind).term(term).index(index).build();
	}
}
