package org.graph.api.core;

import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.specification.RouteSpecification;
import org.graph.api.core.route.specification.RouteSpecificationDefault;

import java.util.ArrayList;
import java.util.List;

public class GraphSpecification<S extends GraphState> {

    private GraphMemory memory;
    private GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects = new ArrayList<>();

    public GraphSpecification<S> memory(GraphMemory memory) {
        this.memory = memory;
        return this;
    }

    public GraphSpecification<S> options(GraphOptions options) {
        this.options = options;
        return this;
    }

    public GraphSpecification<S> aspects(List<? extends NodeAspect<? extends GraphState>> aspects) {
        this.aspects.addAll(aspects);
        return this;
    }

    public GraphSpecification<S> aspect(NodeAspect<? extends GraphState> aspect) {
        this.aspects.add(aspect);
        return this;
    }

    public RouteSpecification<S> begin(Node<? super S> node) {
        return new RouteSpecificationDefault<>(options, memory, node, aspects);
    }
}
