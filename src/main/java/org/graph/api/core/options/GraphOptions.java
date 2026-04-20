package org.graph.api.core.options;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class GraphOptions {

    @NonNull
    private String graphName;

    @Builder.Default
    private int nodeCallLimit = 100;

}
