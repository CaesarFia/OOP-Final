package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.Validators;
import oop.project.library.command.Command;
import oop.project.library.command.CommandException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ArgumentScenarios {

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
        .addParameter(
            "difficulty",
            ArgumentType.string().validate(Validators.choices(List.of("peaceful", "easy", "normal", "hard")))
        ).positional().add()
        .build();

    private static final Command DATE = Command.builder("date")
        .addParameter("date", ArgumentType.custom("LocalDate", LocalDate::parse)).positional().add()
        .build();

    public static Map<String, Object> add(String arguments) throws RuntimeException {
        return parseScenario(ADD, arguments);
    }

    public static Map<String, Object> sub(String arguments) throws RuntimeException {
        return parseScenario(SUB, arguments);
    }

    public static Map<String, Object> fizzbuzz(String arguments) throws RuntimeException {
        return parseScenario(FIZZBUZZ, arguments);
    }

    public static Map<String, Object> difficulty(String arguments) throws RuntimeException {
        return parseScenario(DIFFICULTY, arguments);
    }

    public static Map<String, Object> date(String arguments) throws RuntimeException {
        return parseScenario(DATE, arguments);
    }

    private static Map<String, Object> parseScenario(Command command, String arguments) {
        try {
            return command.parse(arguments).toMap();
        } catch (CommandException e) {
            throw new RuntimeException("Invalid " + command.name() + " command.", e);
        }
    }

}
