package com.snowalker.raft.core.log;

import com.google.common.base.MoreObjects;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/26 23:44
 * @desc 空日志 选举产生新leader之后增加的第一条日志
 */
public class NoOpLogEntry extends AbstractLogEntry {

	// 空日志引用
	public NoOpLogEntry(int term, int index) {
		super(KIND_NO_OP, term, index);
	}

	@Override
	public byte[] getCommandBytes() {
		return new byte[0];
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.toString();
	}
}
