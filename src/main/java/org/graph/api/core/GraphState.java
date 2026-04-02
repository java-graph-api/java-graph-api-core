package org.graph.api.core;

import org.graph.api.core.node.guard.NodeCallState;

import java.util.UUID;

public class GraphState extends NodeCallState {
    // todo все классы состояния должны быть абстрактными и sealed

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

    public final <T extends GraphState> T interrupt() {
        // todo need test
        setExecutorStatus(ExecutorStatus.INTERRUPT);
        return (T) this;
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
