package org.graph.api.core;

import org.graph.api.core.exception.GraphNodeNotFoundException;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.factory.NodeMap;
import org.graph.api.core.route.Route;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeRouting<S extends GraphState> {

    private final Map<String, List<Route<S>>> routes;
    private final NodeMap<S> nodeMap;

    public NodeRouting(Map<String, List<Route<S>>> routes, NodeMap<S> nodeMap) {
        this.routes = Collections.unmodifiableMap(routes);
        this.nodeMap = nodeMap;
    }

    public Route<S> getRoute(Node<?, ?, S> node, Object output, S state) {
        List<Route<S>> conditionalRoutes = getConditionalRoutes(node, output, state);
        return getRouteOrThrow(node, conditionalRoutes);
    }

    public Node<?, ?, S> getNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            return nodeMap.get(nodeName);
        }
        throw new GraphNodeNotFoundException(nodeName);
    }

    public Node<?, ?, S> getBeginNode() {
        var routes = this.routes.get(Route.Type.BEGIN.name());
        if (routes == null || routes.size() != 1) {
            throw GraphRoutingException.routeNotFound(Route.Type.BEGIN.name());
        }
        return routes.get(0).getTarget();
    }

    private List<Route<S>> getConditionalRoutes(Node<?, ?, S> node, Object output, S state) {
        return get(node.getName()).stream()
                .filter(route -> route.test(output, state))
                .toList();
    }

    private List<Route<S>> get(String nodeName) {
        return Objects.requireNonNullElse(routes.get(nodeName), Collections.emptyList());
    }

    private Route<S> getRouteOrThrow(Node<?, ?, S> node, List<Route<S>> conditionalRoutes) {
        if (conditionalRoutes.isEmpty()) {
            throw GraphRoutingException.routeNotFound(node.getName());
        } else if (conditionalRoutes.size() > 1) {
            if (conditionalRoutes.size() == 2) {
                if (conditionalRoutes.get(0).isConditional() && conditionalRoutes.get(1).isDefault()) {
                    return conditionalRoutes.get(0);
                }
            }
            throw GraphRoutingException.multipleRoutesFound(node.getName(), conditionalRoutes);
        } else {
            return conditionalRoutes.get(0);
        }
    }
}
