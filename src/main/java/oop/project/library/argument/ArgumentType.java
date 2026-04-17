package oop.project.library.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

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

    public T parse(String argumentName, String rawValue) throws ArgumentException {
        var value = parser.parse(rawValue);
        for (var validator : validators) {
            validator.validate(argumentName, value);
        }
        return value;
    }

    public ArgumentType<T> validate(Validator<T> validator) {
        Objects.requireNonNull(validator);
        var nextValidators = new ArrayList<>(validators);
        nextValidators.add(validator);
        return new ArgumentType<>(description, parser, List.copyOf(nextValidators));
    }

    public String description() {
        return description;
    }

    public static ArgumentType<Boolean> bool() {
        return new ArgumentType<>("boolean", rawValue -> {
            return switch (rawValue) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new ArgumentException("Expected true or false but received '" + rawValue + "'.");
            };
        }, List.of());
    }

    public static ArgumentType<Integer> integer() {
        return new ArgumentType<>("integer", rawValue -> {
            try {
                return Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                throw new ArgumentException("Expected an integer but received '" + rawValue + "'.", e);
            }
        }, List.of());
    }

    public static ArgumentType<Double> dbl() {
        return new ArgumentType<>("double", rawValue -> {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                throw new ArgumentException("Expected a double but received '" + rawValue + "'.", e);
            }
        }, List.of());
    }

    public static ArgumentType<String> string() {
        return new ArgumentType<>("string", rawValue -> rawValue, List.of());
    }

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
