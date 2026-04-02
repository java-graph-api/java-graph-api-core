package org.graph.api.core.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.graph.api.core.GraphState;
import org.graph.api.core.node.NodeInfo;
import org.graph.api.core.options.GraphOptions;

@Getter
@AllArgsConstructor
public class JoinPoint<S extends GraphState> {

    private final S state;
    private final GraphOptions options;
    private final NodeInfo nodeInfo;

    public String getCurrentNodeName() {
        return nodeInfo.name();
    }

}
