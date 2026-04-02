package org.graph.api.core.model;

import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.GraphState;

@Getter
@Setter
public class OrderState extends GraphState {

    private Order input;

    // validation
    private boolean valid;
    private String status;

    // stock
    private boolean inStock;

    // price & discount
    private double discount;
    private double price;

    // delivery
    private double delivery;

    // final
    private double finalPrice;

    // fraud
    private boolean fraud;

    // auxiliary fields for convenience
    private String itemId;
    private int amount;
    private boolean vip;
    private boolean interrupt;

    public void fromOrder(Order order) {
        this.itemId = order.getItemId();
        this.amount = order.getAmount();
        this.vip = order.isVip();
    }

    public void toInterruptGraph() {
        interrupt = true;
    }
}
