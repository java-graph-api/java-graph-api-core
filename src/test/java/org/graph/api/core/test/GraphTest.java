package org.graph.api.core.test;

import org.graph.api.core.ExecutorStatus;
import org.graph.api.core.GraphBuilder;
import org.graph.api.core.GraphExecutor;
import org.graph.api.core.memory.GraphMemory;
import org.graph.api.core.memory.GraphMemoryDefault;
import org.graph.api.core.model.Order;
import org.graph.api.core.model.OrderState;
import org.graph.api.core.node.ConsumerNode;
import org.graph.api.core.node.SupplierNode;
import org.graph.api.core.node.RunnableNode;
import org.graph.api.core.node.UnaryNode;
import org.graph.api.core.node.action.ConsumerNodeAction;
import org.graph.api.core.options.GraphOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphTest {

    private GraphMemory memory;

    @BeforeEach
    public void setup() {
        memory = new GraphMemoryDefault();
    }

    // 1) validateOrder
    static SupplierNode<Order, OrderState> validateOrder = new SupplierNode<>(
            "validateOrder",
            (state) -> {
                var input = state.getInput();
                state.fromOrder(input);
                boolean ok = input.getAmount() > 0 && input.getItemId() != null && !input.getItemId().isBlank();
                state.setValid(ok);
                return input;
            }
    );

    // rejectOrder (прерывающий)
    static ConsumerNode<Order, OrderState> rejectOrder = new ConsumerNode<>(
            "rejectOrder",
            (input, state) -> {
                state.setStatus("REJECT_INVALID");
                state.toInterruptGraph();
            }
    );

    // 2) checkStock - делает проверку и возвращает тот же Order
    static UnaryNode<Order, OrderState> checkStock = new UnaryNode<>(
            "checkStock",
            (input, state) -> {
                // Для примера — имитация: если hashCode(itemId) чётный -> в наличии
                boolean inStock = input.getItemId().hashCode() % 2 == 0;
                state.setInStock(inStock);
                return input;
            }
    );

    // outOfStock
    static ConsumerNode<Order, OrderState> outOfStock = new ConsumerNode<>(
            "outOfStock",
            (input, state) -> {
                state.setStatus("NO_STOCK");
                state.toInterruptGraph();
            }
    );

    // 3) calculateDiscount
    static UnaryNode<Order, OrderState> calculateDiscount = new UnaryNode<>(
            "calculateDiscount",
            (input, state) -> {
                double d = input.isVip() ? 0.10 : 0.0; // 10% для VIP
                state.setDiscount(d);
                return input;
            }
    );

    // 4) applyDiscount (записывает промежуточную цену)
    static UnaryNode<Order, OrderState> applyDiscount = new UnaryNode<>(
            "applyDiscount",
            (input, state) -> {
                // допустим, базовая цена 100 за единицу
                double basePrice = 100.0 * input.getAmount();
                state.setPrice(basePrice * (1 - state.getDiscount()));
                return input;
            }
    );

    // 5) calculateDelivery
    static UnaryNode<Order, OrderState> calculateDelivery = new UnaryNode<>(
            "calculateDelivery",
            (input, state) -> {
                // статичная стоимость доставки (пример)
                state.setDelivery(300.0);
                return input;
            }
    );

    // 6) finalPrice
    static UnaryNode<Order, OrderState> finalPrice = new UnaryNode<>(
            "finalPrice",
            (input, state) -> {
                state.setFinalPrice(state.getPrice() + state.getDelivery());
                return input;
            }
    );

    // 7) fraudCheck
    static UnaryNode<Order, OrderState> fraudCheck = new UnaryNode<>(
            "fraudCheck",
            (input, state) -> {
                // простой эвристический чек: большие заказы пометить
                state.setFraud(state.getFinalPrice() > 10000);
                return input;
            }
    );

    static ConsumerNode<Order, OrderState> mock = new ConsumerNode<>(
            "mock",
            ConsumerNodeAction.noop()
    );

    // fraudReject
    static ConsumerNode<Order, OrderState> fraudReject = new ConsumerNode<>(
            "fraudReject",
            (input, state) -> {
                state.setStatus("FRAUD_DETECTED");
                state.toInterruptGraph();
            }
    );

    // 8) payment
    static ConsumerNode<Order, OrderState> payment = new ConsumerNode<>(
            "payment",
            (input, state) -> state.setStatus("PAID")
    );

    // 9) finish
    static RunnableNode<OrderState> finish = new RunnableNode<>(
            "finish",
            state -> {
            }
    );

    static ConsumerNode<Order, OrderState> finish2 = new ConsumerNode<>(
            "finish2",
            ConsumerNodeAction.noop()
    );

    public GraphExecutor<OrderState> buildGraph() {
        var options = GraphOptions.builder()
                .graphName("OrderGraph")
                .build();

        return new GraphBuilder<OrderState>()
                .options(options)
                .memory(memory)

                .begin(validateOrder)

                .route(validateOrder, rejectOrder, state -> !state.isValid())
                .route(rejectOrder, finish, OrderState::isInterrupt)
                .route(validateOrder, checkStock)

                .route(checkStock, outOfStock, state -> !state.isInStock())
                .route(checkStock, finish2, OrderState::isInterrupt)
                .route(checkStock, calculateDiscount)

                .route(calculateDiscount, applyDiscount)
                .route(applyDiscount, calculateDelivery)
                .route(calculateDelivery, finalPrice)

                .route(finalPrice, fraudCheck)
                .route(fraudCheck, fraudReject, OrderState::isFraud)
                .route(fraudReject, finish, OrderState::isInterrupt)
                .route(fraudCheck, payment, state -> !state.isFraud())
                .route(fraudCheck, mock)
                .route(mock, finish)

                .route(payment, finish)
                .end(finish);
    }

    @Test
    public void testOrderGraph() {
        var graph = buildGraph();

        Order order1 = new Order("item-even", 2, false, "card"); // itemId с even/hash -> inStock true
        OrderState state1 = new OrderState();
        state1.fromOrder(order1);
        state1.setInput(order1);
        graph.execute(state1, "order-1");

        assertEquals("PAID", state1.getStatus(), "ожидаем оплату или completed в зависимости от nodes");
        assertEquals(ExecutorStatus.COMPLETED, state1.getExecutorStatus());

        Order order2 = new Order(null, 1, false, "card");
        OrderState state2 = new OrderState();
        state2.fromOrder(order2);
        state2.setInput(order2);
        graph.execute(state2, "order-2");

        assertEquals("REJECT_INVALID", state2.getStatus());
        assertEquals(ExecutorStatus.COMPLETED, state2.getExecutorStatus());

        Order big = new Order("item-even", 200, false, "card");
        OrderState state3 = new OrderState();
        state3.fromOrder(big);
        state3.setInput(big);
        graph.execute(state3, "order-3");

        assertEquals("FRAUD_DETECTED", state3.getStatus());
        assertEquals(ExecutorStatus.COMPLETED, state3.getExecutorStatus());
    }
}
