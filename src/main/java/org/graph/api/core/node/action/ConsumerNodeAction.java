package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

/**
 * Действие узла, завершающее выполнение без возвращаемого значения.
 *
 * @param <I> тип входных данных узла
 * @param <S> тип состояния графа
 */
@FunctionalInterface
public interface ConsumerNodeAction<I, S extends GraphState> extends NodeAction<I, Void, S> {

    /**
     * Выполняет завершающее действие узла.
     *
     * @param input входное значение
     * @param state текущее состояние графа
     */
    void complete(I input, S state);

    default Void action(I input, S state) {
        complete(input, state);
        return null;
    }

    /**
     * Создаёт действие, не выполняющее никаких операций.
     *
     * @return действие-заглушку
     */
    static <I, S extends GraphState> ConsumerNodeAction<I, S> noop() {
        return (input, state) -> {
            // void
        };
    }
}
