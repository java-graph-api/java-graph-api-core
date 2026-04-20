package org.graph.api.core.builder;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.point.merge.ReflectionStateMergeStrategy;
import org.graph.api.core.memory.point.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;

import java.util.ArrayList;
import java.util.List;

public class GraphBuilderDefault<S extends GraphState> implements GraphBuilder<S> {

    private GraphMemory memory;
    private GraphOptions options;
    private final List<NodeAspect<? extends GraphState>> aspects = new ArrayList<>();
    private StateMergeStrategy<S> mergeStrategy = new ReflectionStateMergeStrategy<>();

    @Override
    public GraphBuilder<S> memory(GraphMemory memory) {
        this.memory = memory;
        return this;
    }

    @Override
    public GraphBuilder<S> options(GraphOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public GraphBuilder<S> aspects(List<? extends NodeAspect<? extends GraphState>> aspects) {
        this.aspects.addAll(aspects);
        return this;
    }

    @Override
    public GraphBuilder<S> aspect(NodeAspect<? extends GraphState> aspect) {
        this.aspects.add(aspect);
        return this;
    }

    @Override
    public GraphBuilder<S> mergeStrategy(StateMergeStrategy<S> mergeStrategy) {
        this.mergeStrategy = java.util.Objects.requireNonNull(mergeStrategy, "mergeStrategy cannot be null");
        return this;
    }

    @Override
    public GraphDefinitionBuilder<S> begin(Node<? super S> node) {
        return new GraphDefinitionBuilderDefault<>(options, memory, node, aspects, mergeStrategy);
    }
}
