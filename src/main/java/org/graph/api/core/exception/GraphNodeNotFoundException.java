package org.graph.api.core.exception;

public class GraphNodeNotFoundException extends GraphException {

    public GraphNodeNotFoundException(String nodeName) {
        super(String.format("Node '%s' not found", nodeName));
    }
}
