package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

/**
 * Действие узла, преобразующее данные без изменения их типа.
 *
 * @param <D> тип входных и выходных данных
 * @param <S> тип состояния графа
 */
@FunctionalInterface
public interface UnaryNodeAction<D, S extends GraphState> extends NodeAction<D, D, S> {

    /**
     * Применяет преобразование к данным.
     *
     * @param input исходное значение
     * @param state текущее состояние графа
     * @return результат преобразования
     */
    D action(D input, S state);

    /**
     * Возвращает действие, возвращающее входное значение без изменений.
     *
     * @return функция тождественного преобразования
     */
    static <D, S extends GraphState> UnaryNodeAction<D, S> identity() {
        return (input, state) -> input;
    }
}
