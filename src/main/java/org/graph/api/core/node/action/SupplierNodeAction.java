package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

/**
 * Действие узла-поставщика, создающего значение без входных данных.
 *
 * @param <O> тип результата
 * @param <S> тип состояния графа
 */
@FunctionalInterface
public interface SupplierNodeAction<O, S extends GraphState> extends NodeAction<Void, O, S> {

    /**
     * Выполняет генерацию значения, опираясь только на состояние графа.
     *
     * @param state текущее состояние графа
     * @return созданное значение
     */
    O call(S state);

    default O action(Void input, S state) {
        return call(state);
    }

}
