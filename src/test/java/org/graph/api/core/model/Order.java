package org.graph.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String itemId;
    private int amount;
    private boolean vip;
    private String paymentMethod;
}

