package com.snowalker.raft.core.leaderelection.node;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:04
 * @desc 集群中某个具体的节点地址，主要为ip及端口
 */
@AllArgsConstructor
@Getter
public class RaftNodeAddress {

	/**
	 * 当前节点的ip  通常为局域网ip
	 */
	private final String ipAddr;
	/**
	 * 当前节点端口
	 */
	private final int port;

	public static RaftNodeAddress of(String ipAddr, int port) {
		return new RaftNodeAddress(ipAddr, port);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RaftNodeAddress)) {
			return false;
		}
		RaftNodeAddress that = (RaftNodeAddress) o;
		return getPort() == that.getPort() && Objects.equal(getIpAddr(), that.getIpAddr());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getIpAddr(), getPort());
	}
}
