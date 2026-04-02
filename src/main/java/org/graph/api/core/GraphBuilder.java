package org.graph.api.core;

import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.specification.RouteSpecificationDefault;

import java.util.ArrayList;
import java.util.List;

public class GraphBuilder<S extends GraphState> {

    private GraphMemory memory;
    private GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects = new ArrayList<>();

    public GraphBuilder<S> memory(GraphMemory memory) {
        this.memory = memory;
        return this;
    }

    public GraphBuilder<S> options(GraphOptions options) {
        this.options = options;
        return this;
    }

    public GraphBuilder<S> aspects(List<? extends NodeAspect<? extends GraphState>> aspects) {
        this.aspects.addAll(aspects);
        return this;
    }

    public GraphBuilder<S> aspect(NodeAspect<? extends GraphState> aspect) {
        this.aspects.add(aspect);
        return this;
    }

    public RouteSpecificationDefault< S> begin(Node<Void, ?, S> node) {
        return new RouteSpecificationDefault<>(options, memory, node, aspects);
    }
}
