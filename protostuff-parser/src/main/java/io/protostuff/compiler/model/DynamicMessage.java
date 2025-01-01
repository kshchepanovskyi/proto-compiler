package io.protostuff.compiler.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.protostuff.compiler.parser.ParserException;
import io.protostuff.compiler.parser.Util;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Data structure that represents value of an option.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class DynamicMessage implements Map<String, DynamicMessage.Value> {

    public static final char LPAREN = '(';
    public static final char RPAREN = ')';
    public static final char DOT = '.';

    private final Map<Key, Value> fields;

    public DynamicMessage() {
        this.fields = new HashMap<>();
    }

    /**
     * Get option value for a given key (field name or field key - for accessing custom options).
     */
    @Override
    public Value get(Object key) {
        if (key instanceof String) {
            String name = (String) key;
            return get(name);
        }
        return fields.get(key);
    }

    /**
     * Get option value by given option name.
     */
    public Value get(String name) {
        if (name.length() > 1) {
            int dot;
            if (name.charAt(0) == LPAREN) {
                int start = name.indexOf(RPAREN);
                dot = name.indexOf(DOT, start);
            } else {
                dot = name.indexOf(DOT);
            }
            if (dot > 0) {
                String fieldName = name.substring(0, dot);
                String rest = name.substring(dot + 1);
                Key key = createKey(fieldName);
                Value value = fields.get(key);
                if (!value.isMessageType()) {
                    throw new ParserException("Invalid option name: %s", name);
                }
                DynamicMessage msg = value.getMessage();
                return msg.get(rest);
            } else {
                Key key = createKey(name);
                return fields.get(key);
            }
        } else {
            Key key = Key.field(name);
            return fields.get(key);
        }
    }

    public void set(String name, Value value) {
        set(SourceCodeLocation.UNKNOWN, name, value);
    }

    /**
     * Set field of an option to a given value.
     */
    public void set(SourceCodeLocation sourceCodeLocation, String name, Value value) {
        if (name.length() > 1) {
            int dot;
            if (name.charAt(0) == LPAREN) {
                int start = name.indexOf(RPAREN);
                dot = name.indexOf(DOT, start);
            } else {
                dot = name.indexOf(DOT);
            }
            if (dot > 0) {
                String fieldName = name.substring(0, dot);
                String rest = name.substring(dot + 1);
                Key key = createKey(fieldName);
                DynamicMessage msg;
                if (fields.containsKey(key)) {
                    msg = getChildMessage(value, key);
                } else {
                    msg = new DynamicMessage();
                    fields.put(key, Value.createMessage(sourceCodeLocation, msg));
                }
                msg.set(sourceCodeLocation, rest, value);
            } else {
                Key key = createKey(name);
                set(key, value);
            }
        } else {
            Key key = Key.field(name);
            set(key, value);
        }
    }

    private void set(Key key, Value value) {
        if (fields.containsKey(key) && value.isMessageType()) {
            // merge
            Value prevValue = fields.get(key);
            if (!prevValue.isMessageType()) {
                throw new ParserException(value, "Can not set '%s': incompatible type", key);
            }
            DynamicMessage prevMessage = prevValue.getMessage();
            DynamicMessage message = value.getMessage();
            prevMessage.merge(message);
        } else {
            // create new or override previous value
            fields.put(key, value);
        }
    }

    private DynamicMessage getChildMessage(Value value, Key key) {
        DynamicMessage msg;
        Value val = fields.get(key);
        if (!val.isMessageType()) {
            throw new ParserException(value, "Can not assign option value: type error");
        }
        msg = val.getMessage();
        return msg;
    }

    private void merge(DynamicMessage message) {
        for (Map.Entry<Key, Value> entry : message.fields.entrySet()) {
            Key key = entry.getKey();
            Value value = entry.getValue();
            set(key, value);
        }
    }

    private Key createKey(String fieldName) {
        Key key;
        if (fieldName.startsWith("(")) {
            String name = Util.trimStringName(fieldName);
            if (name.startsWith(".")) {
                name = name.substring(1);
            }
            key = Key.extension(name);
        } else {
            key = Key.field(fieldName);
        }
        return key;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fields", fields)
                .toString();
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            String name = (String) key;
            return get(name) != null;
        }
        return fields.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }

    @Override
    public Value put(String key, Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ? extends Value> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nonnull
    public Set<String> keySet() {
        Set<Key> keys = fields.keySet();
        return keys.stream()
                .map(Key::toString)
                .collect(Collectors.toSet());
    }

    @Override
    @Nonnull
    public Collection<Value> values() {
        return fields.values();
    }

    @Override
    @Nonnull
    public Set<Entry<String, Value>> entrySet() {
        Map<String, Value> map = new HashMap<>();
        for (Entry<Key, Value> entry : fields.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return map.entrySet();
    }

    public Set<Entry<Key, Value>> getFields() {
        return fields.entrySet();
    }

    /**
     * Change option name to its fully qualified name.
     */
    public void normalizeName(Key key, String fullyQualifiedName) {
        Value value = fields.remove(key);
        if (value == null) {
            throw new IllegalStateException("Could not find option for key=" + key);
        }
        Key newKey;
        if (fullyQualifiedName.startsWith(".")) {
            // TODO: we should not use format with leading dot internally
            newKey = Key.extension(fullyQualifiedName.substring(1));
        } else {
            newKey = Key.extension(fullyQualifiedName);
        }
        fields.put(newKey, value);
    }

    /**
     * Returns a map of option names as keys and option values as values..
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        for (Entry<Key, Value> keyValueEntry : fields.entrySet()) {
            Key key = keyValueEntry.getKey();
            Value value = keyValueEntry.getValue();
            if (!key.isExtension()) {
                result.put(key.getName(), transformValueToObject(value));
            } else {
                result.put("(" + key.getName() + ")", transformValueToObject(value));
            }
        }
        return result;
    }

    private Object transformValueToObject(Value value) {
        switch (value.getType()) {
            case BOOLEAN:
                return value.getBoolean();
            case INTEGER:
                return value.getInt64();
            case FLOAT:
                return value.getDouble();
            case STRING:
                return value.getString();
            case ENUM:
                return value.getEnumName();
            case MESSAGE:
                return value.getMessage();
            default:
                return value;
        }
    }

    public static class Key {
        private final String name;
        private final boolean extension;

        public Key(String name, boolean extension) {
            this.name = name;
            this.extension = extension;
        }

        public static Key field(String name) {
            return new Key(name, false);
        }

        public static Key extension(String name) {
            return new Key(name, true);
        }

        public String getName() {
            return name;
        }

        public boolean isExtension() {
            return extension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return extension == key.extension
                    && Objects.equals(name, key.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, extension);
        }

        @Override
        public String toString() {
            if (extension) {
                return LPAREN + name + RPAREN;
            }
            return name;
        }
    }

    public static class Value extends AbstractElement {

        private final Element parent;
        private final Type type;
        private final boolean bool;
        private final long number;
        private final double floatNumber;
        private final String string;
        private final String enumName;
        private final DynamicMessage message;

        private Value(SourceCodeLocation sourceCodeLocation, Type type, Object value, Element parent) {
            this.parent = parent;
            this.sourceCodeLocation = sourceCodeLocation;
            this.type = type;
            boolean b = false;
            double f = 0.;
            long l = 0;
            String e = null;
            String s = null;
            DynamicMessage m = null;
            switch (type) {
                case BOOLEAN:
                    b = (Boolean) value;
                    break;
                case INTEGER:
                    l = (Long) value;
                    break;
                case FLOAT:
                    f = (Double) value;
                    break;
                case STRING:
                    s = (String) value;
                    break;
                case ENUM:
                    e = (String) value;
                    break;
                case MESSAGE:
                    m = (DynamicMessage) value;
                    break;
                default:
                    throw new IllegalStateException(String.valueOf(type));
            }
            this.bool = b;
            this.number = l;
            this.floatNumber = f;
            this.string = s;
            this.enumName = e;
            this.message = m;
        }

        public static Value createString(String value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.STRING, value, null);
        }

        public static Value createString(SourceCodeLocation sourceCodeLocation, String value) {
            return new Value(sourceCodeLocation, Type.STRING, value, null);
        }

        public static Value createBoolean(boolean value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.BOOLEAN, value, null);
        }

        public static Value createBoolean(SourceCodeLocation sourceCodeLocation, boolean value) {
            return new Value(sourceCodeLocation, Type.BOOLEAN, value, null);
        }

        public static Value createInteger(long value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.INTEGER, value, null);
        }

        public static Value createInteger(SourceCodeLocation sourceCodeLocation, long value) {
            return new Value(sourceCodeLocation, Type.INTEGER, value, null);
        }

        public static Value createMessage(DynamicMessage value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.MESSAGE, value, null);
        }

        public static Value createMessage(SourceCodeLocation sourceCodeLocation, DynamicMessage value) {
            return new Value(sourceCodeLocation, Type.MESSAGE, value, null);
        }

        public static Value createFloat(double value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.FLOAT, value, null);
        }

        public static Value createFloat(SourceCodeLocation sourceCodeLocation, double value) {
            return new Value(sourceCodeLocation, Type.FLOAT, value, null);
        }

        public static Value createEnum(String value) {
            return new Value(SourceCodeLocation.UNKNOWN, Type.ENUM, value, null);
        }

        public static Value createEnum(SourceCodeLocation sourceCodeLocation, String value) {
            return new Value(sourceCodeLocation, Type.ENUM, value, null);
        }

        @Override
        public Element getParent() {
            return parent;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            switch (type) {
                case BOOLEAN:
                    return String.valueOf(bool);
                case INTEGER:
                    return String.valueOf(number);
                case FLOAT:
                    return String.valueOf(floatNumber);
                case STRING:
                    return String.valueOf(string);
                case ENUM:
                    return String.valueOf(enumName);
                case MESSAGE:
                    return message.toString();
                default:
                    throw new IllegalStateException(String.valueOf(type));
            }
        }


        public boolean getBoolean() {
            Preconditions.checkState(isBooleanType(), "%s is not a boolean", this);
            return bool;
        }

        public boolean isBooleanType() {
            return type == Type.BOOLEAN;
        }

        public long getInt64() {
            Preconditions.checkState(isIntegerType(), "%s is not a number", this);
            return number;
        }

        /**
         * Get option value as {@code int32} number, if option is a numeric value.
         */
        public int getInt32() {
            Preconditions.checkState(isIntegerType(), "%s is not a number", this);
            Preconditions.checkState(number <= Integer.MAX_VALUE, "%s does not fit into int32", number);
            Preconditions.checkState(number >= Integer.MIN_VALUE, "%s does not fit into int32", number);
            return (int) number;
        }

        public double getDouble() {
            return floatNumber;
        }

        public boolean isIntegerType() {
            return type == Type.INTEGER;
        }

        public String getString() {
            Preconditions.checkState(isStringType(), "%s is not a string", this);
            return string;
        }

        public boolean isStringType() {
            return type == Type.STRING;
        }

        public String getEnumName() {
            Preconditions.checkState(isEnumType(), "%s is not a enum", this);
            return enumName;
        }

        public boolean isEnumType() {
            return type == Type.ENUM;
        }


        public DynamicMessage getMessage() {
            Preconditions.checkState(isMessageType(), "%s is not a message", this);
            return message;
        }

        public boolean isMessageType() {
            return type == Type.MESSAGE;
        }

        public enum Type {
            BOOLEAN,
            INTEGER,
            FLOAT,
            STRING,
            ENUM,
            MESSAGE;


            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }
    }
}
