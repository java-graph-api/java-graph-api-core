package org.graph.api.core;

import java.io.Serial;
import java.io.Serializable;

public class ResumableState extends GraphState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int step;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
