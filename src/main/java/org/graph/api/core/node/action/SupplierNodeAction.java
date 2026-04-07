package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

@FunctionalInterface
public interface SupplierNodeAction<O, S extends GraphState> extends NodeAction<Void, O, S> {

    O call(S state);

    default O action(Void input, S state) {
        return call(state);
    }

}
