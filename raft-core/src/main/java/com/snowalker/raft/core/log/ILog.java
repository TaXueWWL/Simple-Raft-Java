package com.snowalker.raft.core.log;

import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesResultMessage;
import com.snowalker.raft.core.leaderelection.protocol.AppendEntriesRpcResponse;

import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/27 20:40
 * @desc Raft Log 基本接口
 */
public interface ILog {

	int ALL_ENTRIES = -1;

	/** 获取最后一个日志的元数据*/
	LogEntryMeta getLastEntryData();

	/**
	 * 日志下一个索引
	 * @param term
	 * @param selfId
	 * @param nextIndex
	 * @param maxEntries
	 * @return
	 */
	AppendEntriesRpcResponse createAppendEntriesRpc(int term, RaftNodeId selfId, int nextIndex, int maxEntries);

	/** (Leader角色)获取下一条日志的索引*/
	int getNextIndex();

	/* (Leader角色)当前日志已提交的索引*/
	int getCommitIndex();

	/**
	 * 提供一个boolean变量，标记一下对象的lastLogIndex和lastLogTerm是否比自己的要新
	 * 该方法主要目的为：在candidate进行选举时，决定是否要进行投票，其实就是看一下投票的RPC请求是否比当前角色持久化的index  term更新
	 * @param lastLogIndex
	 * @param lastLogTerm
	 * @return
	 */
	boolean isNewerThan(int lastLogIndex, int lastLogTerm);

	/* (Leader角色)增加一个NO-OP日志，用于Leader选举成功之后先行发送*/
	NoOpLogEntry appendEntry(int term);

	/* (leader角色)增加一个普通日志，用于日志*/
	GeneralLogEntry appendEntry(int term, byte[] command);

	/* (follower角色)追加来自leader的日志条目*/
	boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<LogEntry> entries);

	/* (follower角色)对日志的commitIndex进行推进*/
	void advanceCommitIndex(int newCommitIndex, int currentTerm);

	/* 关闭日志同步*/
	void close();
}
