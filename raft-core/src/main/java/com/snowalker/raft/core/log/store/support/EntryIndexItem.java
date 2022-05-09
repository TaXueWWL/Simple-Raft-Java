package com.snowalker.raft.core.log.store.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/5/9 23:06
 * @desc
 */
@Getter
public class EntryIndexItem {

	private final int index;
	private final long offset;
	private final int kind;
	private final int term;

	public EntryIndexItem(int index, long offset, int kind, int term) {
		this.index = index;
		this.offset = offset;
		this.kind = kind;
		this.term = term;
	}
}
