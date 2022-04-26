package com.snowalker.raft.core.leaderelection.protocol;

import com.snowalker.raft.core.leaderelection.node.RaftNodeId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
public class AppendEntriesResultMessage {

    private final AppendEntriesRpcResponse result;
    private final RaftNodeId sourceNodeId;
    private final AppendEntriesRpcRequest rpc;

    public AppendEntriesRpcResponse get() {
        return result;
    }

    public RaftNodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public AppendEntriesRpcRequest getRpc() {
        return rpc;
    }
}
