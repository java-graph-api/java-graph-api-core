package org.graph.api.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestMemoryState extends GraphState implements Serializable {

    private int value;
    private boolean pauseDone;
    private List<String> trace = new ArrayList<>();

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isPauseDone() {
        return pauseDone;
    }

    public void setPauseDone(boolean pauseDone) {
        this.pauseDone = pauseDone;
    }

    public List<String> getTrace() {
        return trace;
    }

    public void setTrace(List<String> trace) {
        this.trace = trace;
    }
}
