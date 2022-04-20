package com.snowalker.raft.mock;

import com.snowalker.raft.core.leaderelection.task.ElectionTimeoutTimer;
import com.snowalker.raft.core.leaderelection.task.LogReplicationTask;
import com.snowalker.raft.core.leaderelection.task.Scheduler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/19 11:56
 * @desc 测试使用的定时器
 */
@Slf4j
public class NullScheduler implements Scheduler {

	/**
	 * 创建日志复制定时任务
	 *
	 * @param task
	 * @return
	 */
	@Override
	public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
		log.debug("Schedule log replication task.");
		return LogReplicationTask.NONE;
	}

	/**
	 * 创建选举超时器
	 *
	 * @param task
	 * @return
	 */
	@Override
	public ElectionTimeoutTimer scheduleElectionTimeoutTimer(Runnable task) {
		log.debug("Schedule election timeout timer.");
		return ElectionTimeoutTimer.NONE;
	}

	/**
	 * 关闭定时器
	 *
	 * @throws InterruptedException
	 */
	@Override
	public void stop() throws InterruptedException {
	}
}
