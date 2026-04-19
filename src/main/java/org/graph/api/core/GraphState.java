package org.graph.api.core;

import org.graph.api.core.node.guard.NodeCallState;

import java.util.Objects;
import java.util.UUID;

public abstract class GraphState extends NodeCallState {

    private transient ExecutionContext executionContext;

    public final ExecutionContext getExecutionContext() {
        return ensureExecutionContext();
    }

    public final UUID getExecutionId() {
        return ensureExecutionContext().getExecutionId();
    }

    public final String getSessionId() {
        return ensureExecutionContext().getSessionId();
    }

    public final ExecutorStatus getExecutorStatus() {
        return ensureExecutionContext().getExecutorStatus();
    }

    public final void toInterruptGraph() {
        interruptGraph();
    }

    public final void interruptGraph() {
        ensureExecutionContext().interrupt();
    }

    final void completed() {
        ensureExecutionContext().complete();
    }

    final void initExecutionContext(String sessionId) {
        this.executionContext = ExecutionContext.init(sessionId);
    }

    final boolean isGraphInterrupted() {
        return ensureExecutionContext().isInterrupted();
    }

    private ExecutionContext ensureExecutionContext() {
        this.executionContext = Objects.requireNonNull(
                this.executionContext,
                "ExecutionContext is not initialized. Use GraphExecutor.execute(state, sessionId)"
        );
        return executionContext;
    }
}
