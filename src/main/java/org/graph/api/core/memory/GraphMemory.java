package org.graph.api.core.memory;

import java.util.Optional;

public interface GraphMemory {

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

    void put(SavePoint savePoint);

    Optional<SavePoint> get(String graphName, String sessionId);

}
