package org.graph.api.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.graph.api.core.GraphState;

@Slf4j
public class LoggingAspect implements NodeAspect<GraphState> {

    @Override
    public Object around(ProcessingJoinPoint<GraphState> processingJoinPoint, Object input) {
        log.info("Graph: {}, Node: {} -> begin", processingJoinPoint.getOptions().getGraphName(), processingJoinPoint.getCurrentNodeName());
        try {
            var result = processingJoinPoint.action();
            log.info("Graph: {}, Node: {} -> completed", processingJoinPoint.getOptions().getGraphName(), processingJoinPoint.getCurrentNodeName());
            return result;
        } catch (Throwable e) {
            log.error("Graph: {}, Node: {} -> error: {}", processingJoinPoint.getOptions().getGraphName(), processingJoinPoint.getCurrentNodeName(), e.getMessage());
            throw e;
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
