package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.node.action.SupplierNodeAction;

public class SupplierNode<O, S extends GraphState> extends FunctionalNode<Void, O, S> {

    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction) {
        this(name, nodeAction, 0);
    }

    public SupplierNode(String name, SupplierNodeAction<O, S> nodeAction, int callLimit) {
        super(name, nodeAction, callLimit);
    }

}
