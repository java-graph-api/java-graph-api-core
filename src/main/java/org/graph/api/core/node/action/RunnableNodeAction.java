package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

/**
 * Действие узла без входных и выходных данных, использующее только состояние графа.
 *
 * @param <S> тип состояния графа
 */
@FunctionalInterface
public interface RunnableNodeAction<S extends GraphState> extends ConsumerNodeAction<Void, S> {

    /**
     * Выполняет действие, опираясь только на состояние графа.
     *
     * @param state текущее состояние графа
     */
    void complete(S state);

    default void complete(Void input, S state) {
        action(input, state);
    }

    default Void action(Void input, S state) {
        complete(state);
        return null;
    }

    /**
     * Создаёт действие, не выполняющее никаких операций.
     *
     * @return действие-заглушку
     */
    static <S extends GraphState> RunnableNodeAction<S> noop() {
        return (state) -> {
        };
    }
}
