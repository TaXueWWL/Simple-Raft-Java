package com.snowalker.raft.core.log.sequence;

import com.snowalker.raft.common.exception.SequenceEmptyException;
import com.snowalker.raft.core.log.LogEntry;
import com.snowalker.raft.core.log.LogEntryMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/27 21:15
 * @desc 抽象sequence，封装通用行为，屏蔽差异
 *
 * 初始状态：日志偏移量 = 下一个日志索引 == 1
 */
@Slf4j
public abstract class AbstractEntrySequence implements EntrySequence {

	/**日志索引偏移量*/
	private int logIndexOffset;

	/**下一条日志索引*/
	private int nextLogIndex;

	public AbstractEntrySequence(int logIndexOffset, int nextLogIndex) {
		this.logIndexOffset = logIndexOffset;
		this.nextLogIndex = nextLogIndex;
	}


	/**
	 * 判断日志条目是否为空
	 */
	@Override
	public boolean isEmpty() {
		return logIndexOffset == nextLogIndex;
	}

	/**
	 * 获取第一条日志的索引
	 */
	@Override
	public int getFirstLogIndex() {
		if (isEmpty()) {
			throw new SequenceEmptyException("getFirstLogIndex error, empty logEntries");
		}
		return doGetFirstLogIndex();
	}

	int doGetFirstLogIndex() {
		return logIndexOffset;
	}

	/**
	 * 获取最后一条日志的索引
	 */
	@Override
	public int getLastLogIndex() {
		if (isEmpty()) {
			throw new SequenceEmptyException("getLastLogIndex error, empty logEntries");
		}
		return doGetLastLogIndex();
	}

	int doGetLastLogIndex() {
		return nextLogIndex - 1;
	}

	/**
	 * 获取下一条日志索引
	 */
	@Override
	public int getNextLogIndex() {
		return nextLogIndex;
	}

	/**
	 * 从某个下标开始，截取日志序列 主要目的是为了快速匹配到一批日志，并且批量同步给落后的follower节点
	 * eg: 有一个leader在上一个/更早的term宕机了，于是乎，集群选举出了新的leader，拥有了新的term
	 * 当之前的leader恢复之后，重新加入了Raft集群，于是它需要和当前的leader进行日志同步，这个时候，因为
	 * 他已经差了很多的进度，所以一条一条同步，效率太低（每条日志都需要发一个RPC请求），如何提高效率呢？
	 * <p>
	 * 显而易见，就是使用批处理。
	 * <p>
	 * 也就是说我们一次性获取该leader需要同步的日志，批量通过心跳发送给他就可以达到快速同步进度的目的了。
	 *
	 * 具体如何获取日志序列的子序列，由子类决定
	 * @param fromIndex
	 * @return
	 */
	@Override
	public List<LogEntry> subList(int fromIndex) {

		if (isEmpty() || fromIndex > doGetLastLogIndex()) {
			// 日志序列为空，或者开始下标不合法
			log.warn("return emptylist. isEmpty():{}, fromIndex:{}, lastLogIndex:{}", isEmpty(),fromIndex, doGetLastLogIndex());
			return Collections.emptyList();
		}
		// 日志序列非空且开始下标合法，则截取 max(fromIndex, firstLogIndex) --> fromIndex传负值，也能处理, nextLogIndex的序列
		return subList(Math.max(fromIndex, doGetFirstLogIndex()), nextLogIndex);
	}

	/**
	 * 获取指定范围的批量日志
	 *
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Override
	public List<LogEntry> subList(int fromIndex, int toIndex) {
		return null;
	}

	/**
	 * 日志是否存在(单个日志校验)
	 *
	 * 判断条件：日志序列不为空 && 日志下标 >= firstLogIndex && 日志下标 <= lastLogIndex
	 * @param index
	 */
	@Override
	public boolean isEntryPresent(int index) {
		return !isEmpty() && index >= doGetFirstLogIndex() && index <= doGetLastLogIndex();
	}

	/**
	 * 获取某个下标对应日志的元数据
	 *
	 * @param index
	 */
	@Override
	public LogEntryMeta getEntryMeta(int index) {
		LogEntry logEntry = getEntry(index);
		return logEntry != null ? logEntry.getMeta() : null;
	}

	/**
	 * 获取指定下标的日志条目
	 *
	 * @param index
	 */
	@Override
	public LogEntry getEntry(int index) {
		// 日志存在性校验  卫语句
		if (!isEntryPresent(index)) {
			return null;
		}
		return doGetEntry(index);
	}

	/**
	 * 如何获取日志内容，由具体的子类实现
	 * @param index
	 * @return
	 */
	protected abstract LogEntry doGetEntry(int index);

	/**
	 * 为了方便，增加一个获取最后一条日志的方法
	 */
	@Override
	public LogEntry getLastEntry() {
		return null;
	}

	/**
	 * （单条）追加日志
	 *
	 * @param entry
	 */
	@Override
	public void append(LogEntry entry) {

	}

	/**
	 * （批量）追加日志
	 *
	 * @param entries
	 */
	@Override
	public void batchAppend(List<LogEntry> entries) {

	}

	/**
	 * 推进commitIndex
	 *
	 * @param index
	 */
	@Override
	public void commit(int index) {

	}

	/**
	 * 获取当前commitIndex
	 */
	@Override
	public int getCommitIndex() {
		return 0;
	}

	/**
	 * 移除某个索引之后的日志（方便快速进行日志的截取重放）
	 * 主要用于在追加来自Leader节点的日志，出现日志冲突之后，对现有日志进行移除
	 *
	 * @param index
	 */
	@Override
	public void removeAfter(int index) {

	}

	/**
	 * 关闭日志seq
	 */
	@Override
	public void close() {

	}
}
