package org.graph.api.core.node.guard;

import org.graph.api.core.memory.point.SavePointState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NodeInvocationState extends SavePointState {

    private transient final Map<String, Integer> counter = new ConcurrentHashMap<>();

    final int increment(String nodeName) {
        return counter.merge(nodeName, 1, Integer::sum);
    }

}
