package com.snowalker.raft.core.leaderelection.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/9 19:25
 * @desc 选举超时
 *              本质上是对ScheduledFuture的封装，主要目的为取消超时定时任务
 */
public class ElectionTimeoutTimer {

	public static final ElectionTimeoutTimer NONE = new ElectionTimeoutTimer(new NullScheduledFuture());

	private final ScheduledFuture<?> scheduledFuture;

	public ElectionTimeoutTimer(ScheduledFuture<?> scheduledFuture) {
		this.scheduledFuture = scheduledFuture;
	}

	/**
	 * 取消选举超时
	 */
	public void cancel() {

		// 尝试取消此任务的执行。
		//      如果任务已完成、已被取消或由于某些其他原因无法取消，则此尝试将失败。
		//      如果成功，并且在调用cancel时此任务尚未启动，则此任务不应该运行。
		//      如果任务已经开始，则mayInterruptIfRunning参数确定是否应该中断执行该任务的线程以尝试停止该任务。
		//  此方法返回后，对isDone()的后续调用将始终返回true 。如果此方法返回true ，则后续调用isCancelled()将始终返回true 。
		//  参数：
		//      mayInterruptIfRunning - 如果执行此任务的线程应该被中断，则为true ；否则，允许完成正在进行的任务
		//  对于当前场景：调用cancel之后不中断当前任务，等待执行完成之后，则取消该任务
		this.scheduledFuture.cancel(false);
	}

	@Override
	public String toString() {

		// 选举超时已取消
		if (this.scheduledFuture.isCancelled()) {
			return "ElectionTimeoutTimer(state == canceled)";
		}

		// 选举超时已执行
		if (this.scheduledFuture.isDone()) {
			return "ElectionTimeoutTimer(state == done)";
		}

		// 选举超时任务未执行，打印执行时间（在XX毫秒之后执行）
		return "ElectionTimeoutTimer(delay == " + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + " ms";
	}
}
