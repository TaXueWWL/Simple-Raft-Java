package com.snowalker.raft.core.log;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/26 23:26
 * @desc Raft日志抽象
 */
public interface LogEntry {

	// 日志类型 无操作，成为leader后追加
	int KIND_NO_OP = 0;
	// 日志类型 业务日志
	int KIND_GENERAL = 1;

	// 获取日志类型
	int getKind();

	// 获取索引
	int getIndex();

	// 获取任期
	int getTerm();

	// 获取日志元信息（kind term及index）
	LogEntryMeta getMeta();

	// 获取日志payload
	byte[] getCommandBytes();
}
