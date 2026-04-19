package org.graph.api.core.memory;

import java.util.Map;

public record ClassMetadata(
        Map<String, PropertyMetadata> properties
) {
}
