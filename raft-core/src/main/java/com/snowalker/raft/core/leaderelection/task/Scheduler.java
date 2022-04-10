package com.snowalker.raft.core.leaderelection.task;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/9 23:27
 * @desc 定时器接口 主要提供选举超时、日志复制定时器抽象
 * 主要提供创建、关闭方法，对于选举超时，如果需要重置，则只需要组合创建与关闭即可，先关闭后创建即可实现重置。
 */
public interface Scheduler {

	/**
	 * 创建日志复制定时任务
	 * @param task
	 * @return
	 */
	LogReplicationTask scheduleLogReplicationTask(Runnable task);

	/**
	 * 创建选举超时器
	 * @param task
	 * @return
	 */
	ElectionTimeoutTimer scheduleElectionTimeoutTimer(Runnable task);

	/**
	 * 关闭定时器
	 * @throws InterruptedException
	 */
	void stop() throws InterruptedException;
}
