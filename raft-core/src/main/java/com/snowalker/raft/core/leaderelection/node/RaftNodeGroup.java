package com.snowalker.raft.core.leaderelection.node;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 23:32
 * @desc Raft集群成员映射表  当前节点及其所属的集群映射关系
 */
public class RaftNodeGroup {

	/**
	 * 当前节点id
	 */
	private final RaftNodeId currentNodeId;

	/**
	 * 集群模式
	 */
	private ClusterMode clusterMode;

	/**
	 * raft集群成员id 与 成员元数据 映射关系
	 */
	private Map<RaftNodeId, RaftGroupMemberMetadata> raftGroupMemberMap;

	/**
	 * 单节点构造方法：即单点模式
	 * @param endPoint
	 */
	public RaftNodeGroup(RaftNodeEndPoint endPoint) {
		this(Collections.singleton(endPoint), endPoint.getId());
		this.clusterMode = ClusterMode.SINGLE;
	}

	/**
	 * 多节点构造方法：及集群模式
	 * @param endPoints
	 * @param raftNodeId
	 */
	public RaftNodeGroup(Collection<RaftNodeEndPoint> endPoints, RaftNodeId raftNodeId) {
		this.raftGroupMemberMap = buildRaftGroupMemberMap(endPoints);
		this.currentNodeId = raftNodeId;
		this.clusterMode = ClusterMode.CLUSTER;
	}

	/**
	 * 根据nodeId查找节点元数据 找不到则抛出NPE
	 * 使用场景：Follower接收到客户端请求时，需要将请求转发给Leader所在机器，调用该方法返回当前Group中Leader机器的成员信息
	 *         如果集群刚启动或者选择尚未结束，则Follower节点就不存在有效的leaderNodeId，因此需要抛出异常
	 * @param raftNodeId
	 * @return
	 */
	public RaftGroupMemberMetadata findMemberThrowable(RaftNodeId raftNodeId) {
		RaftGroupMemberMetadata groupNodeMetadata = this.findMemberNormally(raftNodeId);
		if (groupNodeMetadata == null) {
			throw new NullPointerException("RaftNodeMetadata not in this group, id:" + raftNodeId.val());
		}
		return groupNodeMetadata;
	}

	/**
	 * 根据nodeId查找节点元素，找不到则返回NULL
	 * 使用场景：Follower接受到来自Leader的心跳，需要检查leader是否在当前NodeId所在的NodeGroup中，如果不存在则需要打印日志告警
	 *         此时不需要抛出异常
	 * @param raftNodeId
	 * @return
	 */
	public RaftGroupMemberMetadata findMemberNormally(RaftNodeId raftNodeId) {
		return this.raftGroupMemberMap.getOrDefault(raftNodeId, null);
	}

	/**
	 * 枚举日志复制节点，即枚举除本身之外的其他所有节点
	 * @return
	 */
	public Set<RaftNodeEndPoint> listReplicationMembersWithoutSelf() {
		return raftGroupMemberMap.entrySet()
				.stream()
				.filter(groupMember -> !groupMember.getKey().equals(this.currentNodeId))
				.map(Map.Entry::getValue)
				.map(RaftGroupMemberMetadata::getEndPoint)
				.collect(Collectors.toSet());
	}

	/**
	 * List replication target.
	 * <p>Self is not replication target.</p>
	 *
	 * @return replication targets.
	 */
	public Collection<RaftGroupMemberMetadata> listReplicationTarget() {
		return raftGroupMemberMap.values().stream()
				.filter(id -> !id.idEquals(currentNodeId))
				.collect(Collectors.toList());
	}

	/**
	 * 构造Raft集群映射关系Map
	 * 日志复制对象一般来说是除了本身之外的其他所有服务器，因此此处排除与自身NodeId相同的服务器节点
	 * @param endPoints
	 * @return
	 */
	private Map<RaftNodeId, RaftGroupMemberMetadata> buildRaftGroupMemberMap(Collection<RaftNodeEndPoint> endPoints) {
		Map<RaftNodeId, RaftGroupMemberMetadata> map = Maps.newHashMap();
		endPoints.forEach(endPoint -> {
			map.put(endPoint.getId(), new RaftGroupMemberMetadata(endPoint));
		});
		if (map.isEmpty()) {
			throw new IllegalArgumentException("endPoints is empty!");
		}
		return map;
	}

	/**
	 * 获取节点数量
	 * @return
	 */
	public int getCountOfMajor() {
		return (int) raftGroupMemberMap.values().stream().filter(RaftGroupMemberMetadata::isMajor).count();
	}
}
