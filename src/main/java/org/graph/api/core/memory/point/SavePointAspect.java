package org.graph.api.core.memory.point;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.JoinPoint;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;

import java.util.Objects;

public class SavePointAspect implements NodeAspect<GraphState> {

    private final GraphMemory graphMemory;

    public SavePointAspect(GraphMemory graphMemory) {
        this.graphMemory = graphMemory;
    }

    @Override
    public void before(JoinPoint<GraphState> joinPoint) {
        ((SavePointState) joinPoint.getState()).saveClear();
    }

    @Override
    public void after(JoinPoint<GraphState> joinPoint) {
        var state = (SavePointState) joinPoint.getState();
        if (state.isSave()) {
            ensureGraphMemory();
            String nodeName = state.getSaveNodeName() == null ? joinPoint.getCurrentNodeName() : state.getSaveNodeName();
            var graphName = joinPoint.getOptions().getGraphName();
            graphMemory.put(graphName, nodeName, joinPoint.getState(), joinPoint.getState().getSessionId());
        }
    }

    private void ensureGraphMemory() {
        Objects.requireNonNull(graphMemory, "GraphMemory is not initialized");
    }
}
