package com.snowalker.raft.core.log;

import lombok.AllArgsConstructor;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/26 23:32
 * @desc 抽象日志Entry
 */
@AllArgsConstructor
public abstract class AbstractLogEntry implements LogEntry {

	private final int kind;
	private final int term;
	private final int index;

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	public int getTerm() {
		return term;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public LogEntryMeta getMeta() {
		return LogEntryMeta.of(kind, term, index);
	}
}
