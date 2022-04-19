package com.snowalker.raft.core.leaderelection;

import com.google.common.eventbus.EventBus;
import com.snowalker.raft.core.leaderelection.node.IRaftNode;
import com.snowalker.raft.core.leaderelection.node.RaftNodeEndPoint;
import com.snowalker.raft.core.leaderelection.node.RaftNodeGroup;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.task.DefaultScheduler;
import com.snowalker.raft.core.leaderelection.task.Scheduler;
import com.snowalker.raft.core.network.RpcConnector;
import com.snowalker.raft.core.scheduler.SingleThreadTaskExecutor;
import com.snowalker.raft.core.scheduler.TaskExecutor;
import com.snowalker.raft.core.store.FileRaftNodeStore;
import com.snowalker.raft.core.store.MemoryRaftNodeStore;
import com.snowalker.raft.core.store.RaftNodeStore;
import com.snowalker.raft.core.store.StoreType;

import java.util.Collection;
import java.util.Collections;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/19 18:49
 * @desc RaftNode构造器
 */
public class RaftNodeBuilder {

	/**集群成员*/
	private final RaftNodeGroup group;

	/**节点id*/
	private final RaftNodeId selfId;

	private final EventBus eventBus;

	/**定时器*/
	private Scheduler scheduler = null;

	/**RPC连接器*/
	private RpcConnector connector = null;

	/**异步任务线程池*/
	private TaskExecutor taskExecutor = null;

	/**状态存储类型*/
	private StoreType storeType = StoreType.FILE;

	/**
	 * 单节点构造方法
	 * @param endPoint
	 */
	public RaftNodeBuilder(RaftNodeEndPoint endPoint) {
		this(Collections.singletonList(endPoint), endPoint.getId());
	}

	public RaftNodeBuilder(Collection<RaftNodeEndPoint> endPoints, RaftNodeId selfId) {
		this.group = new RaftNodeGroup(endPoints, selfId);
		this.selfId = selfId;
		this.eventBus = new EventBus(selfId.getVal());
	}

	public StoreType getStoreType() {
		return storeType;
	}

	public RaftNodeBuilder setStoreType(StoreType storeType) {
		this.storeType = storeType;
		return this;
	}

	public RaftNodeBuilder setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}

	public RaftNodeBuilder setConnector(RpcConnector connector) {
		this.connector = connector;
		return this;
	}

	public RaftNodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		return this;
	}

	/**
	 * 构建RaftNode实例
	 * @return
	 */
	public IRaftNode build() {
		return new RaftNode(buildContext());
	}

	/**
	 * 具体的构建逻辑 TODO
	 * @return
	 */
	private RaftNodeContext buildContext() {

		RaftNodeContext context = new RaftNodeContext();
		context.setGroup(this.group);
		context.setCurrentId(this.selfId);
		context.setEventBus(this.eventBus);
		// TODO 重构为基于参数配置
		context.setScheduler(scheduler != null ? scheduler : new DefaultScheduler(10, 200, 20, 20));
		context.setConnector(this.connector);
		context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor("node"));

		switch (storeType) {
			case FILE:
				// TODO 从配置读取文件路径
				break;
			case MEMORY:
			default:
				context.setStore(new MemoryRaftNodeStore());
				break;
		}

		return context;
	}
}
