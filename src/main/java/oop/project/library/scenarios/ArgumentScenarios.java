package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.Validators;
import oop.project.library.command.Command;
import oop.project.library.command.CommandException;

import java.time.LocalDate;
import java.util.Map;

public final class ArgumentScenarios {

    private enum Difficulty {
        PEACEFUL,
        EASY,
        NORMAL,
        HARD
    }

    private static final Command ADD = Command.builder("add")
        .addParameter("left", ArgumentType.integer()).positional().add()
        .addParameter("right", ArgumentType.integer()).positional().add()
        .build();

    private static final Command SUB = Command.builder("sub")
        .addParameter("left", ArgumentType.dbl()).positional().add()
        .addParameter("right", ArgumentType.dbl()).positional().add()
        .build();

    private static final Command FIZZBUZZ = Command.builder("fizzbuzz")
        .addParameter("number", ArgumentType.integer().validate(Validators.range(1, 100))).positional().add()
        .build();

    private static final Command DIFFICULTY = Command.builder("difficulty")
        .addParameter("difficulty", ArgumentType.enumeration(Difficulty.class)).positional().add()
        .build();

    private static final Command DATE = Command.builder("date")
        .addParameter("date", ArgumentType.custom("LocalDate", LocalDate::parse)).positional().add()
        .build();

    public static Map<String, Object> add(String arguments) throws RuntimeException {
        try {
            var parsed = ADD.parse(arguments);
            return Map.of(
                "left", parsed.getInt("left"),
                "right", parsed.getInt("right")
            );
        } catch (CommandException e) {
            throw new RuntimeException("Invalid add command.", e);
        }
    }

    public static Map<String, Object> sub(String arguments) throws RuntimeException {
        try {
            var parsed = SUB.parse(arguments);
            return Map.of(
                "left", parsed.getDouble("left"),
                "right", parsed.getDouble("right")
            );
        } catch (CommandException e) {
            throw new RuntimeException("Invalid sub command.", e);
        }
    }

    public static Map<String, Object> fizzbuzz(String arguments) throws RuntimeException {
        try {
            var parsed = FIZZBUZZ.parse(arguments);
            return Map.of("number", parsed.getInt("number"));
        } catch (CommandException e) {
            throw new RuntimeException("Invalid fizzbuzz command.", e);
        }
    }

    public static Map<String, Object> difficulty(String arguments) throws RuntimeException {
        try {
            var parsed = DIFFICULTY.parse(arguments);
            var difficulty = parsed.get("difficulty", Difficulty.class);
            return Map.of("difficulty", difficulty.name().toLowerCase());
        } catch (CommandException e) {
            throw new RuntimeException("Invalid difficulty command.", e);
        }
    }

    public static Map<String, Object> date(String arguments) throws RuntimeException {
        try {
            var parsed = DATE.parse(arguments);
            return Map.of("date", parsed.get("date", LocalDate.class));
        } catch (CommandException e) {
            throw new RuntimeException("Invalid date command.", e);
        }
    }

}
