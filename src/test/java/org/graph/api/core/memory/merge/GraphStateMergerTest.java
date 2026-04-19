package org.graph.api.core.memory.merge;

import lombok.Getter;
import lombok.Setter;
import org.graph.api.core.memory.ClassMetadata;
import org.graph.api.core.memory.PropertyMetadata;
import org.graph.api.core.memory.annotation.SavePointExclude;
import org.graph.api.core.memory.annotation.SavePointIgnore;
import org.graph.api.core.memory.annotation.SavePointReadInclude;
import org.graph.api.core.memory.annotation.SavePointWriteInclude;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphStateMergerTest {

    @Test
    void shouldMergeBeanPropertiesAndBooleanIsGetterByDefault() {
        DefaultState source = new DefaultState();
        source.setName("Alice");
        source.setActive(true);

        DefaultState target = new DefaultState();
        target.setName("Bob");
        target.setActive(false);

        GraphStateMerger.merge(source, target);

        assertEquals("Alice", target.getName());
        assertTrue(target.isActive());
    }

    @Test
    void shouldSkipPropertyWhenGetterExcludedByMethod() {
        GetterExcludedState source = new GetterExcludedState();
        source.setValue("new");

        GetterExcludedState target = new GetterExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldSkipPropertyWhenSetterExcludedByMethod() {
        SetterExcludedState source = new SetterExcludedState();
        source.setValue("new");

        SetterExcludedState target = new SetterExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldApplyFieldExcludeForGetterAndSetter() {
        FieldExcludedState source = new FieldExcludedState();
        source.setValue("new");

        FieldExcludedState target = new FieldExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldAllowMethodExcludeOverrideFieldExclude() {
        MethodPriorityOverFieldState source = new MethodPriorityOverFieldState();
        source.setValue("new");

        MethodPriorityOverFieldState target = new MethodPriorityOverFieldState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("new", target.getValue());
    }

    @Test
    void shouldSupportDeprecatedSavePointIgnore() {
        IgnoreState source = new IgnoreState();
        source.setValue("new");

        IgnoreState target = new IgnoreState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldResolveIncludeKeysAndDefaultsFromField() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(IncludeFieldState.class);
        PropertyMetadata property = metadata.properties().get("name");

        assertTrue(property.writeGetterIncluded());
        assertFalse(property.writeSetterIncluded());
        assertEquals("player_name", property.writeKey());

        assertFalse(property.readGetterIncluded());
        assertTrue(property.readSetterIncluded());
        assertEquals("player_name", property.readKey());
    }

    @Test
    void shouldUsePropertyNameAsDefaultKeyWhenIncludeKeyIsEmpty() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(DefaultKeyIncludeState.class);
        PropertyMetadata property = metadata.properties().get("name");

        assertEquals("name", property.writeKey());
        assertEquals("name", property.readKey());
    }

    @Test
    void shouldUseMethodKeyOverFieldKey() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(MethodKeyPriorityState.class);
        PropertyMetadata property = metadata.properties().get("value");

        assertEquals("getter_key", property.writeKey());
        assertEquals("setter_key", property.readKey());
    }

    @Test
    void shouldFailOnDuplicateWriteKey() {
        assertThrows(IllegalStateException.class, () -> AnnotationResolver.resolveClassMetadata(DuplicateWriteKeyState.class));
    }

    @Test
    void shouldFailOnDuplicateReadKey() {
        assertThrows(IllegalStateException.class, () -> AnnotationResolver.resolveClassMetadata(DuplicateReadKeyState.class));
    }

    @Setter
    @Getter
    private static class DefaultState {
        private String name;
        private boolean active;

    }

    @Setter
    private static class GetterExcludedState {
        private String value;

        @SavePointExclude(setter = false)
        public String getValue() {
            return value;
        }

    }

    @Getter
    private static class SetterExcludedState {
        private String value;

        @SavePointExclude(getter = false)
        public void setValue(String value) {
            this.value = value;
        }
    }

    @Setter
    @Getter
    private static class FieldExcludedState {
        @SavePointExclude
        private String value;

    }

    private static class MethodPriorityOverFieldState {
        @SavePointExclude
        private String value;

        @SavePointExclude(getter = false, setter = false)
        public String getValue() {
            return value;
        }

        @SavePointExclude(getter = false, setter = false)
        public void setValue(String value) {
            this.value = value;
        }
    }

    @SuppressWarnings("deprecation")
    @Setter
    private static class IgnoreState {
        private String value;

        @SavePointIgnore
        public String getValue() {
            return value;
        }

    }

    @Setter
    @Getter
    private static class IncludeFieldState {
        @SavePointWriteInclude(key = "player_name")
        @SavePointReadInclude(key = "player_name")
        private String name;

    }

    @Setter
    @Getter
    private static class DefaultKeyIncludeState {
        @SavePointWriteInclude
        @SavePointReadInclude
        private String name;

    }

    private static class MethodKeyPriorityState {
        @SavePointWriteInclude(key = "field_key")
        @SavePointReadInclude(key = "field_key")
        private String value;

        @SuppressWarnings("unused")
        @SavePointWriteInclude(key = "getter_key")
        public String getValue() {
            return value;
        }

        @SuppressWarnings("unused")
        @SavePointReadInclude(key = "setter_key")
        public void setValue(String value) {
            this.value = value;
        }
    }

    @Setter
    @Getter
    private static class DuplicateWriteKeyState {
        @SavePointWriteInclude(key = "hp")
        private int currentHp;

        @SavePointWriteInclude(key = "hp")
        private int maxHp;

    }

    @Getter
    private static class DuplicateReadKeyState {
        private int currentHp;
        private int maxHp;

        @SuppressWarnings("unused")
        @SavePointReadInclude(key = "hp")
        public void setCurrentHp(int currentHp) {
            this.currentHp = currentHp;
        }

        @SuppressWarnings("unused")
        @SavePointReadInclude(key = "hp")
        public void setMaxHp(int maxHp) {
            this.maxHp = maxHp;
        }
    }
}
