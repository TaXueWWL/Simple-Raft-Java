package com.snowalker.raft.core.log.sequence;

import com.snowalker.raft.core.log.LogEntry;
import com.snowalker.raft.core.log.LogEntryMeta;

import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/27 20:58
 * @desc 日志条目序列
 * 提供两种seq实现，内存/文件
 */
public interface EntrySequence {

	/** 判断日志条目是否为空*/
	boolean isEmpty();

	/** 获取第一条日志的索引*/
	int getFirstLogIndex();

	/** 获取最后一条日志的索引*/
	int getLastLogIndex();

	/** 获取下一条日志索引*/
	int getNextLogIndex();

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
	 * @param fromIndex
	 * @return
	 */
	List<LogEntry> subList(int fromIndex);

	/**
	 * 获取指定范围的批量日志
	 *
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	List<LogEntry> subList(int fromIndex, int toIndex);

	/** 日志是否存在(单个日志校验)*/
	boolean isEntryPresent(int index);

	 /** 获取某个日志的元数据*/
	LogEntryMeta getEntryMeta(int index);

	 /** 获取某个日志条目*/
	LogEntry getEntry(int index);

	/** 为了方便，增加一个获取最后一条日志的方法*/
	LogEntry getLastEntry();

	/** （单条）追加日志*/
	void append(LogEntry entry);

	/** （批量）追加日志*/
	void batchAppend(List<LogEntry> entries);

	/** 推进commitIndex*/
	void commit(int index);

	/** 获取当前commitIndex*/
	int getCommitIndex();

	/**
	 * 移除某个索引之后的日志（方便快速进行日志的截取重放）
	 * 主要用于在追加来自Leader节点的日志，出现日志冲突之后，对现有日志进行移除
	 *
	 * @param index
	 */
	void removeAfter(int index);

	/** 关闭日志seq*/
	void close();

}
