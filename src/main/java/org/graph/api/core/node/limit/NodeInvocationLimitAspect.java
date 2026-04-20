package org.graph.api.core.node.limit;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.JoinPoint;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.exception.NodeInvocationLimitExceededException;

public final class NodeInvocationLimitAspect implements NodeAspect<GraphState> {

    @Override
    public void before(JoinPoint<GraphState> joinPoint) {
        NodeInvocationState invocationCounter = joinPoint.getState();
        var invocationCount = invocationCounter.increment(joinPoint.getCurrentNodeName());
        if (joinPoint.getNodeInfo().invocationLimit() > 0) {
            check(invocationCount, joinPoint.getNodeInfo().invocationLimit(), joinPoint.getCurrentNodeName());
        } else {
            check(invocationCount, joinPoint.getOptions().getNodeInvocationLimit(), joinPoint.getCurrentNodeName());
        }
    }

    private void check(int invocationCount, int limit, String nodeName) {
        if (invocationCount > limit) {
            throw new NodeInvocationLimitExceededException(nodeName, invocationCount);
        }
    }
}
