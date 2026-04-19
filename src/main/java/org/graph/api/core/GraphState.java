package org.graph.api.core;

import org.graph.api.core.node.guard.NodeCallState;

import java.util.UUID;

public abstract class GraphState extends NodeCallState {

    private transient ExecutorStatus executorStatus;
    private String sessionId;
    private final UUID executionId = UUID.randomUUID();

    public final UUID getExecutionId() {
        return executionId;
    }

    public final String getSessionId() {
        return sessionId;
    }

    public final ExecutorStatus getExecutorStatus() {
        return executorStatus;
    }

    public final void toInterruptGraph() {
        setExecutorStatus(ExecutorStatus.INTERRUPT);
    }

    final void completed() {
        setExecutorStatus(ExecutorStatus.COMPLETED);
    }

    final void init(String sessionId) {
        this.executorStatus = null;
        this.sessionId = sessionId;
    }

    final boolean isGraphInterrupted() {
        return this.executorStatus == ExecutorStatus.INTERRUPT;
    }

    private void setExecutorStatus(ExecutorStatus executorStatus) {
        this.executorStatus = executorStatus;
    }
}
