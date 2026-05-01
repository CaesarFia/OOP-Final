package oop.project.library.argument;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Factory methods for reusable argument validators.
 */
public final class Validators {

    private Validators() {}

    /**
     * Creates an inclusive integer range validator.
     *
     * @param min smallest accepted value
     * @param max largest accepted value
     * @return validator that rejects integers outside {@code [min, max]}
     */
    public static ArgumentType.Validator<Integer> range(int min, int max) {
        return comparableRange(min, max);
    }

    /**
     * Creates an inclusive double range validator.
     *
     * @param min smallest accepted value
     * @param max largest accepted value
     * @return validator that rejects doubles outside {@code [min, max]}
     */
    public static ArgumentType.Validator<Double> range(double min, double max) {
        return comparableRange(min, max);
    }

    /**
     * Creates a membership validator for a fixed set of allowed values.
     */
    public static <T> ArgumentType.Validator<T> choices(Collection<? extends T> allowedValues) {
        var allowed = Set.copyOf(allowedValues);
        return (argumentName, value) -> {
            if (!allowed.contains(value)) {
                throw new ArgumentException(
                    "Argument '" + argumentName + "' must be one of " + allowed + "."
                );
            }
        };
    }

    /**
     * Creates a validator that requires the full string to match the supplied regular expression.
     */
    public static ArgumentType.Validator<String> regex(String regex) {
        var pattern = Pattern.compile(regex);
        return (argumentName, value) -> {
            if (!pattern.matcher(value).matches()) {
                throw new ArgumentException(
                    "Argument '" + argumentName + "' must match regex '" + regex + "'."
                );
            }
        };
    }

    private static <T extends Comparable<? super T>> ArgumentType.Validator<T> comparableRange(T min, T max) {
        Objects.requireNonNull(min);
        Objects.requireNonNull(max);
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Range minimum cannot be greater than the maximum.");
        }

        return (argumentName, value) -> {
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                throw new ArgumentException(
                    "Argument '" + argumentName + "' must be between " + min + " and " + max + "."
                );
            }
        };
    }

}
