package org.graph.api.core.memory;

import lombok.Builder;

@Builder
public record SavePoint(
        String graphName,
        String nodeName,
        String sessionId,
        Object state,
        Class<?> stateClass
) {
}