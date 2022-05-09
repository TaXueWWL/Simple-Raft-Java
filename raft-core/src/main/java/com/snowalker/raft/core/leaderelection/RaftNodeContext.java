package com.snowalker.raft.core.leaderelection;

import com.google.common.eventbus.EventBus;
import com.snowalker.raft.core.leaderelection.node.RaftGroupMemberMetadata;
import com.snowalker.raft.core.leaderelection.node.RaftNodeGroup;
import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.task.Scheduler;
import com.snowalker.raft.core.network.RpcConnector;
import com.snowalker.raft.core.scheduler.TaskExecutor;
import com.snowalker.raft.core.log.store.RaftNodeStore;
import lombok.Setter;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 12:24
 * @desc 核心层上下文 封装各种内部组件
 */
@Setter
public class RaftNodeContext {

	/**当前节点id*/
	private RaftNodeId currentId;

	/**Raft成员列表*/
	private RaftNodeGroup group;

//	private Log log;

	/**RPC接口*/
	private RpcConnector connector;

	/**定时器*/
	private Scheduler scheduler;

	/**事件总线*/
	private EventBus eventBus;

	/**主线程执行器，处理逻辑的执行接口*/
	private TaskExecutor taskExecutor;

	/**角色状态存储*/
	private RaftNodeStore store;

	public RaftNodeId currentId() {
		return currentId;
	}

	public RaftNodeGroup group() {
		return group;
	}

	public RpcConnector connector() {
		return connector;
	}

	public Scheduler scheduler() {
		return scheduler;
	}

	public EventBus eventBus() {
		return eventBus;
	}

	public TaskExecutor taskExecutor() {
		return taskExecutor;
	}

	public RaftNodeStore store() {
		return store;
	}

	public RaftGroupMemberMetadata findMember(RaftNodeId sourceNodeId) {
		return group.findMemberNormally(sourceNodeId);
	}
}
