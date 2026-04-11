package oop.project.library.argument;

import java.util.Collection;
import java.util.Set;

public final class Validators {

    private Validators() {}

    public static ArgumentType.Validator<Integer> range(int min, int max) {
        return (argumentName, value) -> {
            if (value < min || value > max) {
                throw new ArgumentException(
                    "Argument '" + argumentName + "' must be between " + min + " and " + max + "."
                );
            }
        };
    }

    public static ArgumentType.Validator<Double> range(double min, double max) {
        return (argumentName, value) -> {
            if (value < min || value > max) {
                throw new ArgumentException(
                    "Argument '" + argumentName + "' must be between " + min + " and " + max + "."
                );
            }
        };
    }

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

}
