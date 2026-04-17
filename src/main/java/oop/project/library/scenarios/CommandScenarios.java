package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentType;
import oop.project.library.command.Command;
import oop.project.library.command.CommandException;

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
            .constValue(true)
            .defaultValue(false)
            .add()
        .build();

    private static final Command DISPATCH_STATIC = Command.builder("static")
        .addParameter("value", ArgumentType.integer()).positional().add()
        .build();

    private static final Command DISPATCH_DYNAMIC = Command.builder("dynamic")
        .addParameter("value", ArgumentType.string()).positional().add()
        .build();

    private static final Command DISPATCH = Command.builder("dispatch")
        .addSubcommand("static", DISPATCH_STATIC)
        .addSubcommand("dynamic", DISPATCH_DYNAMIC)
        .build();

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        try {
            var parsed = MUL.parse(arguments);
            return Map.of(
                "left", parsed.getInt("left"),
                "right", parsed.getInt("right")
            );
        } catch (CommandException e) {
            throw new RuntimeException("Invalid mul command.", e);
        }
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        try {
            var parsed = DIV.parse(arguments);
            return Map.of(
                "left", parsed.getDouble("left"),
                "right", parsed.getDouble("right")
            );
        } catch (CommandException e) {
            throw new RuntimeException("Invalid div command.", e);
        }
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        try {
            var parsed = ECHO.parse(arguments);
            return Map.of("message", parsed.getString("message"));
        } catch (CommandException e) {
            throw new RuntimeException("Invalid echo command.", e);
        }
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        try {
            var parsed = SEARCH.parse(arguments);
            var outputKey = parsed.toMap().containsKey("i") ? "i" : "case-insensitive";
            return Map.of(
                "term", parsed.getString("term"),
                outputKey, parsed.getBoolean("case-insensitive")
            );
        } catch (CommandException e) {
            throw new RuntimeException("Invalid search command.", e);
        }
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        try {
            var parsed = DISPATCH.parse(arguments);
            var type = parsed.getSubcommandName();
            var subcommandArguments = parsed.getSubcommandArguments();
            if (type.equals("dynamic")) {
                return Map.of("type", type, "value", subcommandArguments.getString("value"));
            }
            return Map.of("type", type, "value", subcommandArguments.getInt("value"));
        } catch (CommandException e) {
            throw new RuntimeException("Invalid dispatch command.", e);
        }
    }

}
