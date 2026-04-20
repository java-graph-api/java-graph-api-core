package org.graph.api.core.aspect;

import org.graph.api.core.GraphState;

public interface NodeAspect<S extends GraphState> {

    default void before(JoinPoint<S> joinPoint) {
    }

    default void after(JoinPoint<S> joinPoint) {
    }

    default void around(ProcessingJoinPoint<S> processingJoinPoint) {
        processingJoinPoint.action();
    }

    default int order() { // todo rename to getOrder()
        return 0;
    }
}
