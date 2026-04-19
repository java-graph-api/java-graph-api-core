package org.graph.api.core.node.guard;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.JoinPoint;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.exception.TooManyNodeCallException;

public class NodeCallChecker implements NodeAspect<GraphState> {

    @Override
    public void before(JoinPoint<GraphState> joinPoint) {
        NodeCallState callCounter = joinPoint.getState();
        var calls = callCounter.increment(joinPoint.getCurrentNodeName());
        if (joinPoint.getNodeInfo().callLimit() > 0) {
            check(calls, joinPoint.getNodeInfo().callLimit(), joinPoint.getCurrentNodeName());
        } else {
            check(calls, joinPoint.getOptions().getNodeCallLimit(), joinPoint.getCurrentNodeName());
        }
    }

    private void check(int calls, int limit, String nodeName) {
        if (calls > limit) {
            throw new TooManyNodeCallException(nodeName, calls);
        }
    }
}
