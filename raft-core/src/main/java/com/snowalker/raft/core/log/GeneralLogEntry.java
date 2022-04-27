package com.snowalker.raft.core.log;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/26 23:31
 * @desc 普通日志条目
 */
public class GeneralLogEntry extends AbstractLogEntry {

	/**日志payload*/
	private final byte[] commandBytes;

	public GeneralLogEntry(int term, int index, byte[] commandBytes) {
		super(KIND_GENERAL, term, index);
		this.commandBytes = commandBytes;
	}

	@Override
	public byte[] getCommandBytes() {
		return commandBytes;
	}


	@Override
	public String toString() {
		return toStringHelper(this)
				.add("kind", KIND_GENERAL)
				.add("term", this.getTerm())
				.add("index", this.getIndex())
				.toString();
	}

}
