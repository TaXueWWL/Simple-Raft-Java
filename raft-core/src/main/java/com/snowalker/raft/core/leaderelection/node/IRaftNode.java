package com.snowalker.raft.core.leaderelection.node;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/13 22:59
 * @desc raftNode 核心组件
 */
public interface IRaftNode {

	/**
	 * 启动
	 */
	void start();

	/**
	 * 关闭
	 * @throws InterruptedException
	 */
	void stop() throws InterruptedException;
}
