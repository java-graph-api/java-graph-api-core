package org.graph.api.core.node.factory;

import org.graph.api.core.GraphState;
import org.graph.api.core.aspect.NodeAspect;
import org.graph.api.core.aspect.ProcessingJoinPoint;
import org.graph.api.core.node.Node;
import org.graph.api.core.node.NodeInfo;
import org.graph.api.core.options.GraphOptions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class NodeProxyFactory<S extends GraphState> {

    private final GraphOptions options;

    public NodeProxyFactory(GraphOptions options) {
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    public <I, O> Node<? super S> createProxy(Node<? super S> target, List<NodeAspect<? extends GraphState>> aspects) {
        if (target == null) {
            return null;
        }

        if (aspects.isEmpty()) {
            return target;
        }

        List<NodeAspect<? extends GraphState>> sortedAspects = aspects.stream()
                .sorted(Comparator.comparingInt(NodeAspect::getOrder))
                .toList();

        InvocationHandler handler = (proxy, method, args) -> {
            if (isActionMethod(method)) {
                I input = (I) args[0];
                S state = (S) args[1];

                Consumer<S> chain = buildAspectChain(target, sortedAspects);
                chain.accept(state);
            }

            return defaultMethods(target, method, args);
        };

        return (Node<S>) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{Node.class},
                handler
        );
    }

    private boolean isActionMethod(Method method) {
        return "call".equals(method.getName()) && method.getParameterCount() == 2;
    }

    private <I, O> Object defaultMethods(Node<? super S> target, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "toString":
                return target.toString();
            case "hashCode":
                return target.hashCode();
            case "equals":
                Object other = (args != null && args.length == 1) ? args[0] : null;
                if (other == null) return false;

                if (Proxy.isProxyClass(other.getClass())) {
                    InvocationHandler otherHandler = Proxy.getInvocationHandler(other);
                    //noinspection EqualsBetweenInconvertibleTypes
                    return this.equals(otherHandler);
                }
                return target.equals(other);

            default:
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }
        }
    }

    @SuppressWarnings("unchecked")
    private <I, O, T extends GraphState> Consumer<S> buildAspectChain(Node<? super S> target, List<NodeAspect<? extends GraphState>> aspects) {
        Consumer<S> chain = target::call;

        for (int i = aspects.size() - 1; i >= 0; i--) {
            NodeAspect<T> aspect = (NodeAspect<T>) aspects.get(i);
            Consumer<S> next = chain;

            chain = (state) -> {
                NodeInfo nodeInfo = new NodeInfo(target.getName(), target.callLimit());
                //noinspection unchecked
                T aspectState = (T) state;
                ProcessingJoinPoint<T> joinPoint = new ProcessingJoinPoint<>(aspectState, options, nodeInfo, () -> next.accept(state));
                aspect.before(joinPoint);
                aspect.around(joinPoint);
                aspect.after(joinPoint);

            };
        }

        return chain;
    }

}
