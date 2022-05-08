package com.snowalker.raft.core.store;

import com.snowalker.raft.core.log.GeneralLogEntry;
import com.snowalker.raft.core.log.LogEntry;
import com.snowalker.raft.core.log.NoOpLogEntry;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/8 18:24
 * @desc 日志Eantry工厂
 */
public class LogEntryFactory {

	private LogEntryFactory() {
	}

	private static final LogEntryFactory INSTANCE = new LogEntryFactory();

	public static LogEntryFactory getInstance() {
		return INSTANCE;
	}

	public LogEntry create(int kind, int index, int term, byte[] commandBytes) {
		switch (kind) {
			case LogEntry.KIND_NO_OP:
				return new NoOpLogEntry(index, term);
			case LogEntry.KIND_GENERAL:
				return new GeneralLogEntry(term, index, commandBytes);
			default:
				throw new IllegalArgumentException("Unexpected entry kind:" + kind);
		}
	}

}
