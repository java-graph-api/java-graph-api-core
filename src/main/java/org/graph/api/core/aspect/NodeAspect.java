package org.graph.api.core.aspect;

import org.graph.api.core.GraphState;

public interface NodeAspect<S extends GraphState> {

    default void before(JoinPoint<S> joinPoint, Object input) {
    }

    default void after(JoinPoint<S> joinPoint, Object result) {
    }

    default Object around(ProcessingJoinPoint<S> processingJoinPoint, Object input) {
        return processingJoinPoint.action();
    }

    default int getOrder() {
        return 0;
    }
}
