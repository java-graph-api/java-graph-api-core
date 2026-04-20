package org.graph.api.core.builder;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.merge.StateMergeStrategy;
import org.graph.api.core.node.Node;
import org.graph.api.core.options.GraphOptions;

import java.util.List;

public interface GraphBuilder<S extends GraphState> {

    GraphBuilder<S> memory(GraphMemory memory);

    GraphBuilder<S> options(GraphOptions options);

    GraphBuilder<S> aspects(List<? extends NodeAspect<? extends GraphState>> aspects);

    GraphBuilder<S> aspect(NodeAspect<? extends GraphState> aspect);

    GraphBuilder<S> mergeStrategy(StateMergeStrategy<S> mergeStrategy);

    GraphDefinitionBuilder<S> begin(Node<? super S> node);
}
