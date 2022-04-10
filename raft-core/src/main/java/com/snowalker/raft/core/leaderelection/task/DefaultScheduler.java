package com.snowalker.raft.core.leaderelection.task;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.concurrent.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/9 23:33
 * @desc 提供一个定时器的默认实现，使用singleThreadScheduledExecutor
 */
@Getter
public class DefaultScheduler implements Scheduler {

	/**
	 * 最小选举超时时间
	 */
	private final int minElectionTimeout;
	/**
	 * 最大选举超时时间
	 */
	private final int maxElectionTimeout;
	/**
	 * 初次日志复制延迟时间
	 */
	private final int logReplicationInitialDelay;
	/**
	 * 日志复制时间间隔
	 */
	private final int logReplicationInterval;
	/**
	 * 随机数生成器
	 */
	private final ThreadLocalRandom electionTimeoutRandom;
	/**
	 * 定时调度线程池
	 */
	private final ScheduledExecutorService scheduledExecutorService;

	public DefaultScheduler(int minElectionTimeout,
	                        int maxElectionTimeout,
	                        int logReplicationInitialDelay,
	                        int logReplicationInterval) {

		// 选举超时时间预校验
		Preconditions.checkArgument(minElectionTimeout > 0, "minElectionTimeout should > 0");
		Preconditions.checkArgument(maxElectionTimeout > 0, "maxElectionTimeout should > 0");
		Preconditions.checkArgument(minElectionTimeout <= maxElectionTimeout, "minElectionTimeout should <= maxElectionTimeout");

		// 初次日志复制延迟及日志复制时间间隔预校验
		Preconditions.checkArgument(logReplicationInitialDelay >= 0, "logReplicationInitialDelay should >= 0");
		Preconditions.checkArgument(logReplicationInterval > 0, "logReplicationInterval should > 0");

		this.minElectionTimeout = minElectionTimeout;
		this.maxElectionTimeout = maxElectionTimeout;
		this.logReplicationInitialDelay = logReplicationInitialDelay;
		this.logReplicationInterval = logReplicationInterval;
		this.electionTimeoutRandom = ThreadLocalRandom.current();
		this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "default-scheduler-thread"));
	}

	/**
	 * 创建日志复制定时任务 定时调度
	 *
	 * @param task
	 * @return
	 */
	@Override
	public LogReplicationTask scheduleLogReplicationTask(Runnable task) {

		ScheduledFuture<?> scheduledFuture =
				this.scheduledExecutorService.scheduleWithFixedDelay(task, logReplicationInitialDelay, logReplicationInterval, TimeUnit.MILLISECONDS);

		return new LogReplicationTask(scheduledFuture);
	}

	/**
	 * 创建选举超时器
	 *
	 * @param task
	 * @return
	 */
	@Override
	public ElectionTimeoutTimer scheduleElectionTimeoutTimer(Runnable task) {

		// 生成随机超时时间 = 随机数（最大选择超时-最小选举超时） + 最小选举超时
		// eg: 100 + random(400 - 100) = 120
		int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;

		// 为当前选举term创建一个一次性的ScheduledFuture
		ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(task, timeout, TimeUnit.MILLISECONDS);

		return new ElectionTimeoutTimer(scheduledFuture);
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
