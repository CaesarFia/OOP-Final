package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentException;
import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.Validators;
import oop.project.library.command.Command;
import oop.project.library.command.CommandException;

import java.util.List;
import java.util.Map;

public final class CommandScenarios {

    private static final Command MUL = Command.builder("mul")
        .addParameter("left", ArgumentType.integer()).positional().add()
        .addParameter("right", ArgumentType.integer()).positional().add()
        .build();

    private static final Command DIV = Command.builder("div")
        .addParameter("left", ArgumentType.dbl()).named("left").add()
        .addParameter("right", ArgumentType.dbl()).named("right").add()
        .build();

    private static final Command ECHO = Command.builder("echo")
        .addParameter("message", ArgumentType.string())
            .positional()
            .named("message")
            .defaultValue("echo,echo,echo...")
            .add()
        .build();

    private static final Command SEARCH = Command.builder("search")
        .addParameter("term", ArgumentType.string())
            .positional()
            .named("term")
            .add()
        .addParameter("case-insensitive", ArgumentType.bool())
            .named("case-insensitive")
            .alias("i")
            .defaultValue(false)
            .add()
        .build();

    private static final Command DISPATCH = Command.builder("dispatch")
        .addParameter(
            "type",
            ArgumentType.string().validate(Validators.choices(List.of("static", "dynamic")))
        ).positional().add()
        .addParameter("value", ArgumentType.string()).positional().add()
        .build();

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        return parseScenario(MUL, arguments);
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        return parseScenario(DIV, arguments);
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        return parseScenario(ECHO, arguments);
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        return parseScenario(SEARCH, arguments);
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        try {
            var parsed = DISPATCH.parse(arguments);
            var type = parsed.getString("type");
            var value = parsed.getString("value");
            if (type.equals("dynamic")) {
                return Map.of("type", type, "value", value);
            }
            return Map.of("type", type, "value", ArgumentType.integer().parse("value", value));
        } catch (CommandException | ArgumentException e) {
            throw new RuntimeException("Invalid dispatch command.", e);
        }
    }

    private static Map<String, Object> parseScenario(Command command, String arguments) {
        try {
            return command.parse(arguments).toMap();
        } catch (CommandException e) {
            throw new RuntimeException("Invalid " + command.name() + " command.", e);
        }
    }

}
