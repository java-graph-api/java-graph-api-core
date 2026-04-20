package org.graph.api.core.memory;

import org.graph.api.core.memory.point.SavePoint;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryGraphMemory implements GraphMemory {

    private final Map<String, Map<String, SavePoint>> store = new ConcurrentHashMap<>();

    @Override
    public void put(SavePoint savePoint) {
        var sp = savePointBuilder(savePoint, serialize((Serializable) savePoint.state()));
        store.computeIfAbsent(savePoint.graphName(), v -> new ConcurrentHashMap<>())
                .put(savePoint.sessionId(), sp);
    }

    @Override
    public Optional<SavePoint> get(String graphName, String sessionId) {
        return Optional.ofNullable(store.get(graphName))
                .map(m -> m.get(sessionId))
                .map(savePoint -> savePointBuilder(savePoint, deserialize((byte[]) savePoint.state())));
    }

    private SavePoint savePointBuilder(SavePoint savePoint, Object state) {
        return SavePoint.builder()
                .graphName(savePoint.graphName())
                .nodeName(savePoint.nodeName())
                .sessionId(savePoint.sessionId())
                .state(state)
                .build();
    }

    private byte[] serialize(Serializable object) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos)
        ) {
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Object deserialize(byte[] data) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream in = new ObjectInputStream(bis)
        ) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}
