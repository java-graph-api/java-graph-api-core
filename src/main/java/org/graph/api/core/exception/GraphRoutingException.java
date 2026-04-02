package org.graph.api.core.exception;

public class GraphRoutingException extends GraphException {

    public GraphRoutingException(String nodeName) {
        super(String.format("Route for node '%s' not found", nodeName));
    }

}
