package org.graph.api.core.node;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.aspect.internal.InternalAspectProvider;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.options.GraphOptions;

import java.util.List;

public class NodeFactory<S extends GraphState> {

    private final NodeProxyFactory<S> nodeProxyFactory;
    private final InternalAspectProvider internalAspectProvider;

    public NodeFactory(GraphOptions options, GraphMemory memory) {
        this.nodeProxyFactory = new NodeProxyFactory<>(options);
        this.internalAspectProvider = new InternalAspectProvider(memory);
    }

    public Node<? super S> createProxy(Node<? super S> target, List<NodeAspect<? extends GraphState>> aspects) {
        var proxy = nodeProxyFactory.createProxy(target, internalAspectProvider.get());
        return nodeProxyFactory.createProxy(proxy, aspects);
    }

}
