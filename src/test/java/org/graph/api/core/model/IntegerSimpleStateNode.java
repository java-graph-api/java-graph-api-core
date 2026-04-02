package org.graph.api.core.model;

import org.graph.api.core.SimpleState;
import org.graph.api.core.node.UnaryNode;
import org.graph.api.core.node.action.UnaryNodeAction;

public class IntegerSimpleStateNode extends UnaryNode<Integer, SimpleState> {

    public IntegerSimpleStateNode(String name, UnaryNodeAction<Integer, SimpleState> nodeAction) {
        super(name, nodeAction);
    }

}
