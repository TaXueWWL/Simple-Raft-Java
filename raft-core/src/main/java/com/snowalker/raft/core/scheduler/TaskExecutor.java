package com.snowalker.raft.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 12:29
 * @desc 抽象任务执行器
 */
public interface TaskExecutor {

	/**
	 * 提交任务
	 * @param task
	 * @return
	 */
	Future<?> submit(Runnable task);

	/**
	 * 提交任务  任务有返回值
	 * @param task
	 * @param <V>
	 * @return
	 */
	<V> Future<V> submit(Callable<V> task);

	/**关闭任务执行器*/
	void shutdown() throws InterruptedException;
}
