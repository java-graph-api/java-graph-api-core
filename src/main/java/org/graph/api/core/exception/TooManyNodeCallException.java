package org.graph.api.core.exception;

public class TooManyNodeCallException extends GraphException {

    public TooManyNodeCallException(String nodeName, int calls) {
        super(String.format("Too many node call. Node '%s' calls: %s", nodeName, calls));
    }
}
