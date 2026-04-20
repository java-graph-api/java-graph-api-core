package org.graph.api.core.route;

import org.graph.api.core.GraphState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteMap<S extends GraphState> {

    private final Map<String, List<Route<S>>> routeMap = new HashMap<>();

    public void put(Route<S> route) {
        String key = route.isBegin() ? Route.Type.BEGIN.name() : route.getSource();
        routeMap.computeIfAbsent(key, v -> new ArrayList<>()).add(route);
    }

    public List<Route<S>> get(String key) {
        return routeMap.get(key);
    }
}
