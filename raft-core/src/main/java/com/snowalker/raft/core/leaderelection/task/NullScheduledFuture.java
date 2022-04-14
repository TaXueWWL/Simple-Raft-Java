package com.snowalker.raft.core.leaderelection.task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/14 21:31
 * @desc
 */
public class NullScheduledFuture implements ScheduledFuture<Object> {

	/**
	 * Returns the remaining delay associated with this object, in the
	 * given time unit.
	 *
	 * @param unit the time unit
	 * @return the remaining delay; zero or negative values indicate
	 * that the delay has already elapsed
	 */
	@Override
	public long getDelay(@NotNull TimeUnit unit) {
		return 0;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 *
	 * <p>The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 *
	 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 *
	 * <p>It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 *
	 * <p>In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
	 * <i>expression</i> is negative, zero or positive.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(@NotNull Delayed o) {
		return 0;
	}

	/**
	 * Attempts to cancel execution of this task.  This attempt will
	 * fail if the task has already completed, has already been cancelled,
	 * or could not be cancelled for some other reason. If successful,
	 * and this task has not started when {@code cancel} is called,
	 * this task should never run.  If the task has already started,
	 * then the {@code mayInterruptIfRunning} parameter determines
	 * whether the thread executing this task should be interrupted in
	 * an attempt to stop the task.
	 *
	 * <p>After this method returns, subsequent calls to {@link #isDone} will
	 * always return {@code true}.  Subsequent calls to {@link #isCancelled}
	 * will always return {@code true} if this method returned {@code true}.
	 *
	 * @param mayInterruptIfRunning {@code true} if the thread executing this
	 *                              task should be interrupted; otherwise, in-progress tasks are allowed
	 *                              to complete
	 * @return {@code false} if the task could not be cancelled,
	 * typically because it has already completed normally;
	 * {@code true} otherwise
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	/**
	 * Returns {@code true} if this task was cancelled before it completed
	 * normally.
	 *
	 * @return {@code true} if this task was cancelled before it completed
	 */
	@Override
	public boolean isCancelled() {
		return false;
	}

	/**
	 * Returns {@code true} if this task completed.
	 * <p>
	 * Completion may be due to normal termination, an exception, or
	 * cancellation -- in all of these cases, this method will return
	 * {@code true}.
	 *
	 * @return {@code true} if this task completed
	 */
	@Override
	public boolean isDone() {
		return false;
	}

	/**
	 * Waits if necessary for the computation to complete, and then
	 * retrieves its result.
	 *
	 * @return the computed result
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException    if the computation threw an
	 *                               exception
	 * @throws InterruptedException  if the current thread was interrupted
	 *                               while waiting
	 */
	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return null;
	}

	/**
	 * Waits if necessary for at most the given time for the computation
	 * to complete, and then retrieves its result, if available.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout argument
	 * @return the computed result
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException    if the computation threw an
	 *                               exception
	 * @throws InterruptedException  if the current thread was interrupted
	 *                               while waiting
	 * @throws TimeoutException      if the wait timed out
	 */
	@Override
	public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}
}
