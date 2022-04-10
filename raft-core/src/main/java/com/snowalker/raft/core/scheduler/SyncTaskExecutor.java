package com.snowalker.raft.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 14:51
 * @desc 同步任务执行器
 */
public class SyncTaskExecutor implements TaskExecutor {


	/**
	 * 提交任务
	 *
	 * @param task
	 * @return
	 */
	@Override
	public Future<?> submit(Runnable task) {
		FutureTask<?> futureTask = new FutureTask<>(task, null);
		futureTask.run();
		return futureTask;
	}

	/**
	 * 提交任务  任务有返回值
	 *
	 * @param task
	 * @return
	 */
	@Override
	public <V> Future<V> submit(Callable<V> task) {
		FutureTask<V> futureTask = new FutureTask<>(task);
		futureTask.run();
		return futureTask;
	}

	/**
	 * 关闭任务执行器
	 */
	@Override
	public void shutdown() throws InterruptedException {
 	}
}
