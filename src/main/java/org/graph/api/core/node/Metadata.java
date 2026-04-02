package org.graph.api.core.node;

import lombok.Data;

@Data
public class Metadata {

    private String description;

    public static Metadata empty() {
        return new Metadata();
    }
}
