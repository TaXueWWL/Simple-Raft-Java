package com.snowalker.raft.core.scheduler;

import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.concurrent.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 13:15
 * @desc 默认单线程执行器实现
 */
public class SingleThreadTaskExecutor implements TaskExecutor {

	private static final String DEFAULT_THREAD_NAME = "default-single-thread-task-executor";

	private ExecutorService executorService;

	/**
	 * 默认构造  使用线程亲和
	 */
	public SingleThreadTaskExecutor() {
		this(new AffinityThreadFactory(DEFAULT_THREAD_NAME, AffinityStrategies.SAME_CORE));
	}

	public SingleThreadTaskExecutor(String threadName) {
		this(r -> new Thread(r, threadName));
	}

	public SingleThreadTaskExecutor(ThreadFactory threadFactory) {
		executorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
	}


	/**
	 * 提交任务
	 *
	 * @param task
	 * @return
	 */
	@Override
	public Future<?> submit(Runnable task) {
		return this.executorService.submit(task);
	}

	/**
	 * 提交任务  任务有返回值
	 *
	 * @param task
	 * @return
	 */
	@Override
	public <V> Future<V> submit(Callable<V> task) {
		return this.executorService.submit(task);
	}

	/**
	 * 关闭任务执行器
	 */
	@Override
	public void shutdown() throws InterruptedException {
		executorService.shutdown();
		// 在关闭请求后阻塞，直到所有任务都完成执行，或者发生超时，或者当前线程被中断，以先发生者为准。
		//  参数：
		//      timeout - 等待的最长时间
		//      unit - 超时参数的时间单位
		//  返回：
		//      如果此执行程序终止，则为true ；如果在终止前超时，则为false
		//  抛出：
		//       InterruptedException - 如果在等待时被中断
		executorService.awaitTermination(1, TimeUnit.SECONDS);
	}
}
