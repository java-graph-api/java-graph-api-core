package org.graph.api.core.exception;

import org.graph.api.core.GraphState;
import org.graph.api.core.route.Route;

import java.util.List;

public class GraphRoutingException extends GraphException {

    public GraphRoutingException(String message) {
        super(message);
    }

    public static GraphRoutingException routeNotFound(String nodeName) {
        var message = String.format("Route for node '%s' not found", nodeName);
        return new GraphRoutingException(message);
    }

    public static <S extends GraphState> GraphRoutingException multipleRoutesFound(String nodeName, List<Route<S>> routes) {
        var routesFiltered = routes;

        if (routesFiltered.size() > 2) {
            routesFiltered = routesFiltered.stream()
                    .filter(Route::isConditional)
                    .toList();
        }

        var routesInfo = routesFiltered.stream()
                .map(route -> route.getSource() + " -> " + route.getTarget())
                .toList();

        var message = String.format("Multiple routes found for node '%s': %s", nodeName, routesInfo);
        return new GraphRoutingException(message);
    }
}
