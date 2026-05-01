package oop.project.library.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable typed parse result produced by {@link Command}.
 */
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

    /**
     * Convenience getter for integer arguments.
     */
    public int getInt(String name) throws CommandException {
        return get(name, Integer.class);
    }

    /**
     * Convenience getter for double arguments.
     */
    public double getDouble(String name) throws CommandException {
        return get(name, Double.class);
    }

    /**
     * Convenience getter for boolean arguments.
     */
    public boolean getBoolean(String name) throws CommandException {
        return get(name, Boolean.class);
    }

    /**
     * Convenience getter for string arguments.
     */
    public String getString(String name) throws CommandException {
        return get(name, String.class);
    }

    /**
     * Returns the parsed value for the given canonical argument name, cast to the expected type.
     * Prefer the typed convenience methods ({@link #getInt}, {@link #getString}, etc.) for common
     * types.
     *
     * @param name canonical parameter name declared on the command definition
     * @param expectedType expected runtime class of the parsed value
     * @return the typed parsed value
     * @throws CommandException if the value is missing or does not match the requested type
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

    /**
     * Returns the presentation map used by the scenario harness, preserving the named key that
     * appeared in input when aliases are involved.
     */
    public Map<String, Object> toMap() {
        return displayValues;
    }

    public boolean hasSubcommand() {
        return subcommandName != null;
    }

    /**
     * Returns the selected subcommand name when this parse result came from a command tree.
     */
    public String getSubcommandName() throws CommandException {
        if (subcommandName == null) {
            throw new CommandException("No parsed subcommand exists.");
        }
        return subcommandName;
    }

    /**
     * Returns the nested parsed arguments for the selected subcommand.
     */
    public ParsedArguments getSubcommandArguments() throws CommandException {
        if (subcommandArguments == null) {
            throw new CommandException("No parsed subcommand arguments exist.");
        }
        return subcommandArguments;
    }

}
