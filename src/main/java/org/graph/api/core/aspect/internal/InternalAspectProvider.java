package org.graph.api.core.aspect.internal;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.SavePointAspect;
import org.graph.api.core.node.guard.NodeCallLimitAspect;

import java.util.ArrayList;
import java.util.List;

public final class InternalAspectProvider {

    private final List<NodeAspect<? extends GraphState>> internalAspects = new ArrayList<>(2);

    public InternalAspectProvider(GraphMemory memory) {
        internalAspects.add(new SavePointAspect(memory));
        internalAspects.add(new NodeCallLimitAspect());
    }

    public List<NodeAspect<? extends GraphState>> get() {
        return internalAspects;
    }
}
