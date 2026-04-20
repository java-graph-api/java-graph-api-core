package org.graph.api.core;

import org.graph.api.core.exception.GraphNodeNotFoundException;
import org.graph.api.core.exception.GraphRoutingException;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.NodeMap;
import org.graph.api.core.route.Route;
import org.graph.api.core.route.RouteMap;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class NodeRouting<S extends GraphState> {

    private final RouteMap<S> routeMap;
    private final NodeMap<S> nodeMap;

    public NodeRouting(RouteMap<S> routeMap, NodeMap<S> nodeMap) {
        this.routeMap = routeMap;
        this.nodeMap = nodeMap;
    }

    public Route<S> getRoute(Node<S> node, S state) {
        List<Route<S>> conditionalRoutes = getConditionalRoutes(node, state);
        return getRouteOrThrow(node, conditionalRoutes);
    }

    public Node<? super S> getNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            return nodeMap.get(nodeName);
        }
        throw new GraphNodeNotFoundException(nodeName);
    }

    public Node<? super S> getBeginNode() {
        var routes = this.routeMap.get(Route.Type.BEGIN.name());
        if (routes == null || routes.size() != 1) {
            throw GraphRoutingException.routeNotFound(Route.Type.BEGIN.name());
        }
        return nodeMap.get(routes.get(0).getTarget());
    }

    private List<Route<S>> getConditionalRoutes(Node<S> node, S state) {
        return get(node.getName()).stream()
                .filter(route -> route.test(state))
                .toList();
    }

    private List<Route<S>> get(String nodeName) {
        return Objects.requireNonNullElse(routeMap.get(nodeName), Collections.emptyList());
    }

    private Route<S> getRouteOrThrow(Node<S> node, List<Route<S>> conditionalRoutes) {
        if (conditionalRoutes.isEmpty()) {
            throw GraphRoutingException.routeNotFound(node.getName());
        } else if (conditionalRoutes.size() > 1) {
            if (conditionalRoutes.size() == 2) {
                if (conditionalRoutes.get(0).isConditional() && conditionalRoutes.get(1).isDefault()) {
                    return conditionalRoutes.get(0);
                }
                if (conditionalRoutes.get(1).isConditional() && conditionalRoutes.get(0).isDefault()) {
                    return conditionalRoutes.get(1);
                }
            }
            throw GraphRoutingException.multipleRoutesFound(node.getName(), conditionalRoutes);
        } else {
            return conditionalRoutes.get(0);
        }
    }
}
