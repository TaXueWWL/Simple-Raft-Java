package com.snowalker.raft.core.leaderelection.node;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 22:56
 * @desc 服务器Id 标识集群内的一个节点
 */
@AllArgsConstructor
@Getter
public class RaftNodeId implements Serializable {

	private final String val;

	public static RaftNodeId of(String val) {
		return new RaftNodeId(val);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RaftNodeId)) {
			return false;
		}
		RaftNodeId nodeId = (RaftNodeId) o;
		return Objects.equal(val, nodeId.val);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(val);
	}

	/**
	 * 转换为string
	 * @return
	 */
	public String val() {
		return this.val;
	}

	@Override
	public String toString() {
		return "RaftNodeId{" +
				"val='" + val + '\'' +
				'}';
	}
}
