package org.graph.api.core.memory;

import org.graph.api.core.memory.point.SavePoint;

import java.util.Optional;

public interface GraphMemory {

    void put(SavePoint savePoint);

    Optional<SavePoint> get(String graphName, String sessionId);

    default void put(String graphName, String nodeName, Object state, String sessionId) {
        var savePoint = SavePoint.builder()
                .graphName(graphName)
                .nodeName(nodeName)
                .sessionId(sessionId)
                .state(state)
                .stateClass(state.getClass())
                .build();

        this.put(savePoint);
    }

}
