package oop.project.library.command;

import oop.project.library.argument.ArgumentException;
import oop.project.library.argument.ArgumentType;
import oop.project.library.input.BasicArgs;
import oop.project.library.input.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Command {

    private final String name;
    private final List<Parameter<?>> parameters;
    private final Map<String, Command> subcommands;

    private Command(String name, List<Parameter<?>> parameters, Map<String, Command> subcommands) {
        this.name = name;
        this.parameters = parameters;
        this.subcommands = subcommands;
    }

    public String name() {
        return name;
    }

    public ParsedArguments parse(String input) throws CommandException {
        try {
            return parse(new Input(input).parseBasicArgs());
        } catch (RuntimeException e) {
            throw new CommandException("Unable to parse input for command '" + name + "'.", e);
        }
    }

    public ParsedArguments parse(BasicArgs basicArgs) throws CommandException {
        if (!subcommands.isEmpty()) {
            return parseSubcommand(basicArgs);
        }

        var canonicalValues = new LinkedHashMap<String, Object>();
        var displayValues = new LinkedHashMap<String, Object>();
        var consumedNamed = new HashSet<String>();
        var resolvedParameters = new HashSet<Parameter<?>>();

        for (var parameter : parameters) {
            var matchedKey = parameter.findMatchedKey(basicArgs.named());
            if (matchedKey != null) {
                var parsedValue = parseNamed(parameter, basicArgs.named().get(matchedKey));
                canonicalValues.put(parameter.name(), parsedValue);
                displayValues.put(matchedKey, parsedValue);
                consumedNamed.add(matchedKey);
                resolvedParameters.add(parameter);
            }
        }

        if (consumedNamed.size() != basicArgs.named().size()) {
            var unknownKeys = new ArrayList<String>();
            for (var key : basicArgs.named().keySet()) {
                if (!consumedNamed.contains(key)) {
                    unknownKeys.add(key);
                }
            }
            throw new CommandException("Unknown named arguments for '" + name + "': " + unknownKeys + ".");
        }

        var positionalIndex = 0;
        for (var parameter : parameters) {
            if (resolvedParameters.contains(parameter)) {
                continue;
            }

            if (parameter.positional()) {
                if (positionalIndex < basicArgs.positional().size()) {
                    var parsedValue = parseRaw(parameter, basicArgs.positional().get(positionalIndex));
                    canonicalValues.put(parameter.name(), parsedValue);
                    displayValues.put(parameter.name(), parsedValue);
                    positionalIndex++;
                    continue;
                }
            }

            if (parameter.hasDefaultValue()) {
                canonicalValues.put(parameter.name(), parameter.defaultValue());
                displayValues.put(parameter.name(), parameter.defaultValue());
            } else {
                throw new CommandException("Missing required argument '" + parameter.name() + "' for '" + name + "'.");
            }
        }

        if (positionalIndex != basicArgs.positional().size()) {
            var extras = basicArgs.positional().subList(positionalIndex, basicArgs.positional().size());
            throw new CommandException("Too many positional arguments for '" + name + "': " + extras + ".");
        }

        return new ParsedArguments(canonicalValues, displayValues);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private ParsedArguments parseSubcommand(BasicArgs basicArgs) throws CommandException {
        if (!parameters.isEmpty()) {
            throw new CommandException("Commands with subcommands cannot also define direct parameters.");
        }

        if (basicArgs.positional().isEmpty()) {
            throw new CommandException("Missing required subcommand for '" + name + "'.");
        }

        var selector = basicArgs.positional().get(0);
        var subcommand = subcommands.get(selector);
        if (subcommand == null) {
            throw new CommandException("Unknown subcommand '" + selector + "' for '" + name + "'.");
        }

        var remainingPositionals = List.copyOf(basicArgs.positional().subList(1, basicArgs.positional().size()));
        var subcommandArgs = subcommand.parse(new BasicArgs(remainingPositionals, basicArgs.named()));
        return new ParsedArguments(
            Collections.emptyMap(),
            Collections.emptyMap(),
            selector,
            subcommandArgs
        );
    }

    private static Object parseNamed(Parameter<?> parameter, String rawValue) throws CommandException {
        if (rawValue.isEmpty()) {
            if (parameter.hasConstValue()) {
                return parameter.constValue();
            }
            throw new CommandException("Named argument '" + parameter.name() + "' is missing a value.");
        }
        return parseRaw(parameter, rawValue);
    }

    private static void putNamedToken(java.util.Map<String, String> named, String name, String value) {
        if (named.putIfAbsent(name, value) != null) {
            throw new RuntimeException("Duplicate named argument " + name + ".");
        }
    }

    private static Object parseRaw(Parameter<?> parameter, String rawValue) throws CommandException {
        try {
            return parameter.type().parse(parameter.name(), rawValue);
        } catch (ArgumentException e) {
            throw new CommandException("Invalid value for argument '" + parameter.name() + "'.", e);
        }
    }

    public static final class Builder {

        private final String name;
        private final List<Parameter<?>> parameters = new ArrayList<>();
        private final Set<String> namedKeys = new HashSet<>();
        private final Map<String, Command> subcommands = new LinkedHashMap<>();

        private Builder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public <T> ParameterBuilder<T> addParameter(String name, ArgumentType<T> type) {
            return new ParameterBuilder<>(this, name, type);
        }

        public Builder addSubcommand(String subcommandName, Command command) {
            Objects.requireNonNull(subcommandName);
            Objects.requireNonNull(command);
            if (!parameters.isEmpty()) {
                throw new IllegalStateException("Cannot add subcommands after parameters have been registered.");
            }
            if (subcommands.putIfAbsent(subcommandName, command) != null) {
                throw new IllegalArgumentException("Subcommand '" + subcommandName + "' is already registered.");
            }
            return this;
        }

        private <T> Builder register(Parameter<T> parameter) {
            if (!subcommands.isEmpty()) {
                throw new IllegalStateException("Cannot add parameters to a command that already has subcommands.");
            }
            for (var namedKey : parameter.namedKeys()) {
                if (!namedKeys.add(namedKey)) {
                    throw new IllegalArgumentException("Named key '" + namedKey + "' is already registered.");
                }
            }
            parameters.add(parameter);
            return this;
        }

        public Command build() {
            return new Command(name, List.copyOf(parameters), Map.copyOf(subcommands));
        }
    }

    public static final class ParameterBuilder<T> {

        private final Builder owner;
        private final String name;
        private final ArgumentType<T> type;
        private boolean positional;
        private final List<String> namedKeys = new ArrayList<>();
        private boolean hasDefaultValue;
        private T defaultValue;
        private boolean hasConstValue;
        private T constValue;

        private ParameterBuilder(Builder owner, String name, ArgumentType<T> type) {
            this.owner = owner;
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
        }

        public ParameterBuilder<T> positional() {
            positional = true;
            return this;
        }

        public ParameterBuilder<T> named(String namedKey) {
            namedKeys.add(Objects.requireNonNull(namedKey));
            return this;
        }

        public ParameterBuilder<T> alias(String namedKey) {
            return named(namedKey);
        }

        public ParameterBuilder<T> defaultValue(T value) {
            hasDefaultValue = true;
            defaultValue = value;
            return this;
        }

        public ParameterBuilder<T> constValue(T value) {
            hasConstValue = true;
            constValue = value;
            return this;
        }

        public Builder add() {
            if (!positional && namedKeys.isEmpty()) {
                throw new IllegalStateException("Argument '" + name + "' must be positional and/or named.");
            }
            return owner.register(
                new Parameter<>(
                    name,
                    type,
                    positional,
                    List.copyOf(namedKeys),
                    hasDefaultValue,
                    defaultValue,
                    hasConstValue,
                    constValue
                )
            );
        }
    }

    private static final class Parameter<T> {

        private final String name;
        private final ArgumentType<T> type;
        private final boolean positional;
        private final List<String> namedKeys;
        private final boolean hasDefaultValue;
        private final T defaultValue;
        private final boolean hasConstValue;
        private final T constValue;

        private Parameter(
            String name,
            ArgumentType<T> type,
            boolean positional,
            List<String> namedKeys,
            boolean hasDefaultValue,
            T defaultValue,
            boolean hasConstValue,
            T constValue
        ) {
            this.name = name;
            this.type = type;
            this.positional = positional;
            this.namedKeys = namedKeys;
            this.hasDefaultValue = hasDefaultValue;
            this.defaultValue = defaultValue;
            this.hasConstValue = hasConstValue;
            this.constValue = constValue;
        }

        private String name() {
            return name;
        }

        private ArgumentType<T> type() {
            return type;
        }

        private boolean positional() {
            return positional;
        }

        private List<String> namedKeys() {
            return namedKeys;
        }

        private boolean hasDefaultValue() {
            return hasDefaultValue;
        }

        private T defaultValue() {
            return defaultValue;
        }

        private boolean hasConstValue() {
            return hasConstValue;
        }

        private T constValue() {
            return constValue;
        }

        private String findMatchedKey(java.util.Map<String, String> namedArguments) throws CommandException {
            String matchedKey = null;
            for (var namedKey : namedKeys) {
                if (namedArguments.containsKey(namedKey)) {
                    if (matchedKey != null) {
                        throw new CommandException(
                            "Argument '" + name + "' was provided multiple times using named aliases."
                        );
                    }
                    matchedKey = namedKey;
                }
            }
            return matchedKey;
        }
    }

}
