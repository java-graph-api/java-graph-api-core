package org.graph.api.core;

import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.merge.ReflectionStateMergeStrategy;
import org.graph.api.core.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;
import org.graph.api.core.route.RouteSpecification;
import org.graph.api.core.route.RouteSpecificationDefault;

import java.util.ArrayList;
import java.util.List;

public class GraphSpecification<S extends GraphState> {

    private GraphMemory memory;
    private GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects = new ArrayList<>();
    private StateMergeStrategy<S> mergeStrategy = new ReflectionStateMergeStrategy<>();

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

    public GraphSpecification<S> mergeStrategy(StateMergeStrategy<S> mergeStrategy) {
        this.mergeStrategy = java.util.Objects.requireNonNull(mergeStrategy, "mergeStrategy cannot be null");
        return this;
    }

    public RouteSpecification<S> begin(Node<? super S> node) {
        return new RouteSpecificationDefault<>(options, memory, node, aspects, mergeStrategy);
    }
}
