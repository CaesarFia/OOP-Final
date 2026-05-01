package oop.project.library.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Describes how a single raw string argument is parsed into a typed value.
 *
 * <p>{@code ArgumentType<T>} owns two responsibilities that travel together in this library:
 * converting a raw token into {@code T}, and validating the parsed result before it reaches a
 * command consumer.</p>
 */
public final class ArgumentType<T> {

    @FunctionalInterface
    public interface Parser<T> {
        T parse(String rawValue) throws ArgumentException;
    }

    @FunctionalInterface
    public interface Validator<T> {
        void validate(String argumentName, T value) throws ArgumentException;
    }

    private final String description;
    private final Parser<T> parser;
    private final List<Validator<T>> validators;

    private ArgumentType(String description, Parser<T> parser, List<Validator<T>> validators) {
        this.description = description;
        this.parser = parser;
        this.validators = validators;
    }

    /**
     * Parses one raw argument value and then runs every validator registered on this type.
     *
     * @param argumentName canonical argument name used for validator error messages
     * @param rawValue raw token extracted from command input
     * @return the parsed and validated typed value
     * @throws ArgumentException if parsing fails or any validator rejects the parsed value
     */
    public T parse(String argumentName, String rawValue) throws ArgumentException {
        var value = parser.parse(rawValue);
        for (var validator : validators) {
            validator.validate(argumentName, value);
        }
        return value;
    }

    /**
     * Returns a new argument type that applies the additional validator after parsing.
     */
    public ArgumentType<T> validate(Validator<T> validator) {
        Objects.requireNonNull(validator);
        var nextValidators = new ArrayList<>(validators);
        nextValidators.add(validator);
        return new ArgumentType<>(description, parser, List.copyOf(nextValidators));
    }

    public String description() {
        return description;
    }

    /**
     * Creates a boolean argument type that accepts only {@code true} or {@code false}.
     */
    public static ArgumentType<Boolean> bool() {
        return new ArgumentType<>("boolean", rawValue -> {
            return switch (rawValue) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new ArgumentException("Expected true or false but received '" + rawValue + "'.");
            };
        }, List.of());
    }

    /**
     * Creates an integer argument type backed by {@link Integer#parseInt(String)}.
     */
    public static ArgumentType<Integer> integer() {
        return new ArgumentType<>("integer", rawValue -> {
            try {
                return Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                throw new ArgumentException("Expected an integer but received '" + rawValue + "'.", e);
            }
        }, List.of());
    }

    /**
     * Creates a double argument type backed by {@link Double#parseDouble(String)}.
     */
    public static ArgumentType<Double> dbl() {
        return new ArgumentType<>("double", rawValue -> {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                throw new ArgumentException("Expected a double but received '" + rawValue + "'.", e);
            }
        }, List.of());
    }

    /**
     * Creates a pass-through string argument type.
     */
    public static ArgumentType<String> string() {
        return new ArgumentType<>("string", rawValue -> rawValue, List.of());
    }

    /**
     * Creates an enum-backed argument type that matches constant names case-insensitively.
     */
    public static <E extends Enum<E>> ArgumentType<E> enumeration(Class<E> enumType) {
        Objects.requireNonNull(enumType);
        var constants = enumType.getEnumConstants();
        if (constants == null) {
            throw new IllegalArgumentException(enumType.getName() + " is not an enum type.");
        }

        var allowedValues = new StringJoiner(", ");
        for (var constant : constants) {
            allowedValues.add(constant.name());
        }

        return new ArgumentType<>("enum " + enumType.getSimpleName(), rawValue -> {
            for (var constant : constants) {
                if (constant.name().equalsIgnoreCase(rawValue)) {
                    return constant;
                }
            }
            throw new ArgumentException(
                "Expected one of [" + allowedValues + "] but received '" + rawValue + "'."
            );
        }, List.of());
    }

    /**
     * Creates an argument type from a custom parser.
     *
     * <p>This is the escape hatch for domain-specific values such as {@code LocalDate}. Runtime
     * failures from the supplied parser are normalized into {@link ArgumentException} so callers
     * still interact with one checked parse/validation failure type.</p>
     *
     * @param description human-readable value description used in error messages
     * @param parser parser that converts one raw string into a typed value
     * @return a reusable argument type wrapping the supplied parser
     */
    public static <T> ArgumentType<T> custom(String description, Parser<T> parser) {
        Objects.requireNonNull(description);
        Objects.requireNonNull(parser);
        return new ArgumentType<>(description, rawValue -> {
            try {
                return parser.parse(rawValue);
            } catch (ArgumentException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ArgumentException(
                    "Expected a valid " + description + " but received '" + rawValue + "'.",
                    e
                );
            }
        }, List.of());
    }

}
