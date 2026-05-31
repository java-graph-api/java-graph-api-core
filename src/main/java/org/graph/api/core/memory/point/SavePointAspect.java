package org.graph.api.core.memory.point;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.aspect.ProcessingJoinPoint;
import org.graph.api.core.memory.GraphMemory;

import java.util.Objects;

public class SavePointAspect implements NodeAspect<GraphState> {

    private final GraphMemory graphMemory;

    public SavePointAspect(GraphMemory graphMemory) {
        this.graphMemory = graphMemory;
    }

    @Override
    public void around(ProcessingJoinPoint<GraphState> processingJoinPoint) {
        try {
            ((SavePointState) processingJoinPoint.getState()).saveClear();
            NodeAspect.super.around(processingJoinPoint);
        } finally {
            var state = (SavePointState) processingJoinPoint.getState();
            if (state.isSave() || processingJoinPoint.getNodeInfo().isAlwaysSaved()) {
                ensureGraphMemory();
                String nodeName = state.getSaveNodeName() == null
                        ? processingJoinPoint.getCurrentNodeName()
                        : state.getSaveNodeName();
                var graphName = processingJoinPoint.getOptions().getGraphName();
                var graphState = (GraphState) state;
                var executionId = graphState.getExecutionId().toString();
                graphMemory.put(graphName, nodeName, graphState, graphState.getSessionId(), executionId);
            }
        }
    }

    private void ensureGraphMemory() {
        Objects.requireNonNull(graphMemory, "GraphMemory is not initialized");
    }
}
