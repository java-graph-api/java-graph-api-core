package org.graph.api.core.node.factory;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.Node;

import java.util.HashMap;
import java.util.Map;

public class NodeMap<S extends GraphState> {

    private final Map<String, Node<?, ?, S>> nodeMap = new HashMap<>();

    public void put(String key, Node<?, ?, S> value) {
        if (this.containsKey(key)) {
            var thisValue = this.get(key);
            if (!thisValue.getId().equals(value.getId())) {
                throw new IllegalArgumentException(String.format("Different nodes have the same name '%s'", key));
            }
        } else {
            nodeMap.put(key, value);
        }
    }

    public Node<?, ?, S> get(String key) {
        return nodeMap.get(key);
    }

    public boolean containsKey(String key) {
        return nodeMap.containsKey(key);
    }

}
