package org.graph.api.core.node;

/**
 * Описание узла, объединяющее человекочитаемое имя и ограничение на количество вызовов.
 *
 * @param name      имя узла, используемое в логах и при отладке
 * @param callLimit максимальное количество вызовов узла; {@code 0} означает отсутствие ограничения
 */
public record NodeInfo(String name, int callLimit) {
}
