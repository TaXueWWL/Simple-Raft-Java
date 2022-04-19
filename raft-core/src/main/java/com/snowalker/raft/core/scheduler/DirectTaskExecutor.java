package com.snowalker.raft.core.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 14:51
 * @desc 同步任务执行器
 */
public class DirectTaskExecutor implements TaskExecutor {

	private final boolean throwExceptionWhenFailed;

	public DirectTaskExecutor(boolean throwExceptionWhenFailed) {
		this.throwExceptionWhenFailed = throwExceptionWhenFailed;
	}

	public DirectTaskExecutor() {
		this(false);
	}

	/**
	 * 提交任务
	 *
	 * @param task
	 * @return
	 */
	@Override
	public Future<?> submit(Runnable task) {
		Preconditions.checkNotNull(task);
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
		Preconditions.checkNotNull(task);
		FutureTask<V> futureTask = new FutureTask<>(task);
		futureTask.run();
		return futureTask;
	}

	@Override
	public void submit(Runnable task, Collection<FutureCallback<?>> callbacks) {
		Preconditions.checkNotNull(task);
		Preconditions.checkNotNull(callbacks);
		try {
			task.run();
			callbacks.forEach(c -> c.onSuccess(null));
		} catch (Throwable t) {
			callbacks.forEach(c -> c.onFailure(t));
			if (throwExceptionWhenFailed) {
				throw t;
			}
		}
	}

	/**
	 * 关闭任务执行器
	 */
	@Override
	public void shutdown() throws InterruptedException {
 	}
}
