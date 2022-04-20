package com.snowalker.raft.core.leaderelection.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:03
 * @desc 集群中某个具体的节点
 * 主要属性为id及ip port
 */
@Getter
@AllArgsConstructor
public class RaftNodeEndPoint {

	private final RaftNodeId id;
	private final RaftNodeAddress address;

	public RaftNodeEndPoint(String nodeId, String ipAddress, int port) {
		this(RaftNodeId.of(nodeId), RaftNodeAddress.of(ipAddress, port));
	}

	public static RaftNodeEndPoint of(String nodeId, String ipAddress, int port) {
		return new RaftNodeEndPoint(nodeId, ipAddress, port);
	}

}
