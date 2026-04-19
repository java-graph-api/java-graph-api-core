package org.graph.api.core;

import java.util.Objects;
import java.util.UUID;

public final class ExecutionContext {

    private final String sessionId;
    private final UUID executionId;
    private ExecutorStatus executorStatus;

    private ExecutionContext(String sessionId, UUID executionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.executionId = Objects.requireNonNull(executionId, "executionId cannot be null");
    }

    public static ExecutionContext init(String sessionId) {
        return new ExecutionContext(sessionId, UUID.randomUUID());
    }

    public String getSessionId() {
        return sessionId;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public ExecutorStatus getExecutorStatus() {
        return executorStatus;
    }

    public void interrupt() {
        this.executorStatus = ExecutorStatus.INTERRUPT;
    }

    public void complete() {
        this.executorStatus = ExecutorStatus.COMPLETED;
    }

    public boolean isInterrupted() {
        return this.executorStatus == ExecutorStatus.INTERRUPT;
    }
}
