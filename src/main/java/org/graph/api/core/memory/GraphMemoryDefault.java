package org.graph.api.core.memory;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GraphMemoryDefault implements GraphMemory {

    private final Map<String, Map<String, SavePoint>> store = new ConcurrentHashMap<>();

    @Override
    public void put(SavePoint savePoint) {
        var sp = SavePoint.builder()
                .graphName(savePoint.graphName())
                .nodeName(savePoint.nodeName())
                .sessionId(savePoint.sessionId())
                .state(serialize((Serializable) savePoint.state()))
                .build();
        store.computeIfAbsent(savePoint.graphName(), v -> new ConcurrentHashMap<>())
                .put(savePoint.sessionId(), sp);
    }

    @Override
    public Optional<SavePoint> get(String graphName, String sessionId) {
        return Optional.ofNullable(store.get(graphName))
                .map(m -> m.get(sessionId))
                .map(savePoint -> SavePoint.builder()
                        .graphName(savePoint.graphName())
                        .nodeName(savePoint.nodeName())
                        .sessionId(savePoint.sessionId())
                        .state(deserialize((byte[]) savePoint.state()))
                        .build());
    }

    private static byte[] serialize(Serializable object) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos)
        ) {
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object deserialize(byte[] data) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream in = new ObjectInputStream(bis)
        ) {
            return in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
