package org.graph.api.core.node.guard;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.JoinPoint;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.exception.TooManyNodeCallException;

/**
 * Аспект, контролирующий количество вызовов узла и выбрасывающий исключение при превышении лимита.
 *
 * <p>Использует {@link NodeCallState} из контекста {@link JoinPoint} для подсчёта вызовов и
 * учитывает ограничение, заданное либо в {@link org.graph.api.core.node.NodeInfo},
 * либо в глобальных настройках графа.</p>
 */
public class NodeCallChecker implements NodeAspect<GraphState> {

    /**
     * Проверяет количество вызовов до выполнения узла.
     *
     * @param joinPoint объединённый контекст текущего вызова узла
     * @param input     входное значение, переданное узлу
     */
    @Override
    public void before(JoinPoint<GraphState> joinPoint, Object input) {
        NodeCallState callCounter = joinPoint.getState();
        var calls = callCounter.increment(joinPoint.getCurrentNodeName());
        if (joinPoint.getNodeInfo().callLimit() > 0) {
            check(calls, joinPoint.getNodeInfo().callLimit(), joinPoint.getCurrentNodeName());
        } else {
            check(calls, joinPoint.getOptions().getNodeCallLimit(), joinPoint.getCurrentNodeName());
        }
    }

    private void check(int calls, int limit, String nodeName) {
        if (calls > limit) {
            throw new TooManyNodeCallException(nodeName, calls);
        }
    }
}
