package oop.project.library.command;

import oop.project.library.argument.ArgumentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CommandLibraryTests {

    @Test
    void duplicateNamedKeysAreRejected() {
        var builder = Command.builder("search")
            .addParameter("first", ArgumentType.bool()).named("i").add();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            builder.addParameter("second", ArgumentType.bool()).named("i").add()
        );
    }

    @Test
    void subcommandsSupportDifferentArgumentStructures() throws CommandException {
        var staticCommand = Command.builder("static")
            .addParameter("value", ArgumentType.integer()).positional().add()
            .build();
        var dynamicCommand = Command.builder("dynamic")
            .addParameter("value", ArgumentType.string()).positional().add()
            .build();
        var dispatch = Command.builder("dispatch")
            .addSubcommand("static", staticCommand)
            .addSubcommand("dynamic", dynamicCommand)
            .build();

        var staticParsed = dispatch.parse("dispatch static 1".substring("dispatch".length()));
        Assertions.assertEquals("static", staticParsed.getSubcommandName());
        Assertions.assertEquals(1, staticParsed.getSubcommandArguments().getInt("value"));

        var dynamicParsed = dispatch.parse("dispatch dynamic one".substring("dispatch".length()));
        Assertions.assertEquals("dynamic", dynamicParsed.getSubcommandName());
        Assertions.assertEquals("one", dynamicParsed.getSubcommandArguments().getString("value"));
    }

    @Test
    void aliasCanStillBeObservedInDisplayMap() throws CommandException {
        var search = Command.builder("search")
            .addParameter("term", ArgumentType.string()).positional().add()
            .addParameter("case-insensitive", ArgumentType.bool()).named("case-insensitive").alias("i").add()
            .build();

        var parsed = search.parse(" apple --i true");
        Assertions.assertEquals(true, parsed.getBoolean("case-insensitive"));
        Assertions.assertEquals(Map.of("term", "apple", "i", true), parsed.toMap());
    }

    @Test
    void sameParameterCannotBeProvidedThroughMultipleAliases() {
        var search = Command.builder("search")
            .addParameter("term", ArgumentType.string()).positional().add()
            .addParameter("case-insensitive", ArgumentType.bool())
                .named("case-insensitive")
                .alias("i")
                .constValue(true)
                .add()
            .build();

        Assertions.assertThrows(CommandException.class, () ->
            search.parse(" apple -i --case-insensitive true")
        );
    }

}
