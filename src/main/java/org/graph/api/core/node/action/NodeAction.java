package org.graph.api.core.node.action;

import org.graph.api.core.GraphState;

/**
 * Базовый функциональный интерфейс для действий узла графа.
 *
 * @param <I> тип входных данных, получаемых узлом
 * @param <O> тип результата, возвращаемого узлом
 * @param <S> тип состояния графа, доступного во время выполнения
 */
@FunctionalInterface
public interface NodeAction<I, O, S extends GraphState> {

    /**
     * Выполняет основное действие узла.
     *
     * @param input входное значение, переданное узлу
     * @param state текущее состояние графа
     * @return результат выполнения действия
     */
    O action(I input, S state);
}
