package org.graph.api.core;

import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.memory.SavePointIgnore;
import org.graph.api.core.model.UserRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimpleState extends GraphState implements Serializable {

    private Integer inputWithoutSave;
    private Integer input;
    private UserRequest userRequest;
    private boolean conditional;
    private Integer number;
    private Integer result;
    private boolean evenNumber;
    private boolean answer;
    private String info;
    private List<String> steps = new ArrayList<>();
    private int counter;
    private boolean interrupt;
    private boolean serialize;
    private Boolean serialize1;
    private boolean notSerialize;

    public void countIncrement() {
        counter++;
    }

    public void toInterruptGraphCustom() {
        interrupt = true;
    }

    @SavePointIgnore
    public void setInputWithoutSave(Integer inputWithoutSave) {
        this.inputWithoutSave = inputWithoutSave;
    }

    @SavePointIgnore
    @SuppressWarnings("unused")
    public boolean isNotSerialize() {
        return notSerialize;
    }

    @SavePointIgnore
    public void setNotSerialize(boolean notSerialize) {
        this.notSerialize = notSerialize;
    }
}
