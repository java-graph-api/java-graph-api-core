package org.graph.api.core;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class ResumableState extends GraphState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int step;

}
