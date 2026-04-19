package org.graph.api.core;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class TestMemoryState extends GraphState implements Serializable {

    private int value;
    private boolean pauseDone;
    private List<String> trace = new ArrayList<>();

}
