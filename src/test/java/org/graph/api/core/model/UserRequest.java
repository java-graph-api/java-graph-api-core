package org.graph.api.core.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRequest implements Serializable {
    private boolean answer;
    private int number;

    public UserRequest(int number) {
        this.number = number;
    }
}
