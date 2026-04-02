package org.graph.api.core.model;

import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.GraphState;
import org.graph.api.core.memory.SavePointIgnore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AgentState extends GraphState implements Serializable {

    private List<String> questions;
    private String response;
    private String request;
    private Map<String, String> intentAnswers = new HashMap<>();
    private boolean intentCompleted;
    private boolean graphCompletion;

    @SavePointIgnore
    public String getRequest() {
        return request;
    }

    @SavePointIgnore
    public void setRequest(String request) {
        this.request = request;
    }
}