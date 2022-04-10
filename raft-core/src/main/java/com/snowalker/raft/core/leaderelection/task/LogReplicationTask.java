package com.snowalker.raft.core.leaderelection.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/9 22:58
 * @desc Leader日志同步task
 */
public class LogReplicationTask {

	private final ScheduledFuture<?> scheduledFuture;

	public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
		this.scheduledFuture = scheduledFuture;
	}

	/**
	 * 取消日志复制定时器
	 */
	public void cancel() {
		this.scheduledFuture.cancel(false);
	}

	@Override
	public String toString() {
		return "LogReplicationTask{ delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + " }";
	}
}
