package org.graph.api.core.exception;

public class NodeInvocationLimitExceededException extends GraphException {

    public NodeInvocationLimitExceededException(String nodeName, int invocationCount) {
        super(String.format("Node invocation limit exceeded. Node '%s' invocations: %s", nodeName, invocationCount));
    }
}
