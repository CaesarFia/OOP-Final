package oop.project.library.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ParsedArguments {

    private final Map<String, Object> values;

    ParsedArguments(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public int getInt(String name) throws CommandException {
        return get(name, Integer.class);
    }

    public double getDouble(String name) throws CommandException {
        return get(name, Double.class);
    }

    public boolean getBoolean(String name) throws CommandException {
        return get(name, Boolean.class);
    }

    public String getString(String name) throws CommandException {
        return get(name, String.class);
    }

    public <T> T get(String name, Class<T> expectedType) throws CommandException {
        var value = values.get(name);
        if (value == null) {
            throw new CommandException("No parsed value exists for '" + name + "'.");
        }
        if (!expectedType.isInstance(value)) {
            throw new CommandException(
                "Argument '" + name + "' is a " + value.getClass().getSimpleName()
                    + ", not a " + expectedType.getSimpleName() + "."
            );
        }
        return expectedType.cast(value);
    }

    public Map<String, Object> toMap() {
        return values;
    }

}
