package oop.project.library.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ParsedArguments {

    private final Map<String, Object> canonicalValues;
    private final Map<String, Object> displayValues;
    private final String subcommandName;
    private final ParsedArguments subcommandArguments;

    ParsedArguments(Map<String, Object> canonicalValues, Map<String, Object> displayValues) {
        this(canonicalValues, displayValues, null, null);
    }

    ParsedArguments(
        Map<String, Object> canonicalValues,
        Map<String, Object> displayValues,
        String subcommandName,
        ParsedArguments subcommandArguments
    ) {
        this.canonicalValues = Collections.unmodifiableMap(new LinkedHashMap<>(canonicalValues));
        this.displayValues = Collections.unmodifiableMap(new LinkedHashMap<>(displayValues));
        this.subcommandName = subcommandName;
        this.subcommandArguments = subcommandArguments;
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

    /**
     * Returns the parsed value for the given argument name, cast to the expected type.
     * Throws {@link CommandException} if no value exists for that name or if the stored
     * value is not an instance of {@code expectedType}. Prefer the typed convenience
     * methods ({@link #getInt}, {@link #getString}, etc.) for common types.
     *
     * @param name the canonical parameter name as declared in the command definition
     * @param expectedType the class to cast the stored value to
     * @return the parsed value cast to T
     * @throws CommandException if the argument is missing or is the wrong type
     */
    public <T> T get(String name, Class<T> expectedType) throws CommandException {
        var value = canonicalValues.get(name);
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
        return displayValues;
    }

    public boolean hasSubcommand() {
        return subcommandName != null;
    }

    public String getSubcommandName() throws CommandException {
        if (subcommandName == null) {
            throw new CommandException("No parsed subcommand exists.");
        }
        return subcommandName;
    }

    public ParsedArguments getSubcommandArguments() throws CommandException {
        if (subcommandArguments == null) {
            throw new CommandException("No parsed subcommand arguments exist.");
        }
        return subcommandArguments;
    }

}
