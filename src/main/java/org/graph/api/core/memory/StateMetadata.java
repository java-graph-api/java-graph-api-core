package org.graph.api.core.memory;

import org.graph.api.core.memory.merge.PropertyMetadata;

import java.util.Map;

public record StateMetadata(
        Map<String, PropertyMetadata> properties
) {
}
