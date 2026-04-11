package oop.project.library.command;

import oop.project.library.argument.ArgumentException;
import oop.project.library.argument.ArgumentType;
import oop.project.library.input.BasicArgs;
import oop.project.library.input.Input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Command {

    private final String name;
    private final List<Parameter<?>> parameters;

    private Command(String name, List<Parameter<?>> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String name() {
        return name;
    }

    public ParsedArguments parse(String input) throws CommandException {
        try {
            return parse(tokenize(input));
        } catch (RuntimeException e) {
            throw new CommandException("Unable to parse input for command '" + name + "'.", e);
        }
    }

    public ParsedArguments parse(BasicArgs basicArgs) throws CommandException {
        var values = new LinkedHashMap<String, Object>();
        var consumedNamed = new HashSet<String>();

        for (var parameter : parameters) {
            var matchedKey = parameter.findMatchedKey(basicArgs.named());
            if (matchedKey != null) {
                values.put(parameter.name(), parseNamed(parameter, basicArgs.named().get(matchedKey)));
                consumedNamed.add(matchedKey);
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
            if (values.containsKey(parameter.name())) {
                continue;
            }

            if (parameter.positional()) {
                if (positionalIndex < basicArgs.positional().size()) {
                    values.put(parameter.name(), parseRaw(parameter, basicArgs.positional().get(positionalIndex)));
                    positionalIndex++;
                    continue;
                }
            }

            if (parameter.hasDefaultValue()) {
                values.put(parameter.name(), parameter.defaultValue());
            } else {
                throw new CommandException("Missing required argument '" + parameter.name() + "' for '" + name + "'.");
            }
        }

        if (positionalIndex != basicArgs.positional().size()) {
            var extras = basicArgs.positional().subList(positionalIndex, basicArgs.positional().size());
            throw new CommandException("Too many positional arguments for '" + name + "': " + extras + ".");
        }

        return new ParsedArguments(values);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private BasicArgs tokenize(String input) {
        var tokenizer = new Input(input);
        var positional = new ArrayList<String>();
        var named = new LinkedHashMap<String, String>();

        while (true) {
            switch (tokenizer.parseValue().orElse(null)) {
                case null -> {
                    return new BasicArgs(List.copyOf(positional), java.util.Map.copyOf(named));
                }
                case Input.Value.Literal(String value) -> positional.add(value);
                case Input.Value.QuotedString(String value) -> positional.add(value);
                case Input.Value.SingleFlag(String name) -> putNamedToken(named, name, "");
                case Input.Value.DoubleFlag(String name) -> {
                    var parameter = findNamedParameter(name);
                    var checkpoint = tokenizer.checkpoint();
                    switch (tokenizer.parseValue().orElse(null)) {
                        case Input.Value.Literal(String value) -> {
                            if (shouldConsumeValue(parameter, value)) {
                                putNamedToken(named, name, value);
                            } else {
                                tokenizer.restore(checkpoint);
                                putNamedToken(named, name, "");
                            }
                        }
                        case Input.Value.QuotedString(String value) -> {
                            if (shouldConsumeValue(parameter, value)) {
                                putNamedToken(named, name, value);
                            } else {
                                tokenizer.restore(checkpoint);
                                putNamedToken(named, name, "");
                            }
                        }
                        case null -> putNamedToken(named, name, "");
                        default -> {
                            tokenizer.restore(checkpoint);
                            putNamedToken(named, name, "");
                        }
                    }
                }
            }
        }
    }

    private static Object parseNamed(Parameter<?> parameter, String rawValue) throws CommandException {
        if (rawValue.isEmpty()) {
            if (parameter.type().isBooleanType()) {
                return parseRaw(parameter, "true");
            }
            throw new CommandException("Named argument '" + parameter.name() + "' is missing a value.");
        }
        return parseRaw(parameter, rawValue);
    }

    private Parameter<?> findNamedParameter(String namedKey) {
        for (var parameter : parameters) {
            if (parameter.namedKeys().contains(namedKey)) {
                return parameter;
            }
        }
        return null;
    }

    private static boolean shouldConsumeValue(Parameter<?> parameter, String value) {
        if (parameter == null) {
            return true;
        }
        if (!parameter.type().isBooleanType()) {
            return true;
        }
        return value.equals("true") || value.equals("false");
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

        private Builder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public <T> ParameterBuilder<T> addParameter(String name, ArgumentType<T> type) {
            return new ParameterBuilder<>(this, name, type);
        }

        private <T> Builder register(Parameter<T> parameter) {
            for (var namedKey : parameter.namedKeys()) {
                if (!namedKeys.add(namedKey)) {
                    throw new IllegalArgumentException("Named key '" + namedKey + "' is already registered.");
                }
            }
            parameters.add(parameter);
            return this;
        }

        public Command build() {
            return new Command(name, List.copyOf(parameters));
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

        public Builder add() {
            if (!positional && namedKeys.isEmpty()) {
                throw new IllegalStateException("Argument '" + name + "' must be positional and/or named.");
            }
            return owner.register(
                new Parameter<>(name, type, positional, List.copyOf(namedKeys), hasDefaultValue, defaultValue)
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

        private Parameter(
            String name,
            ArgumentType<T> type,
            boolean positional,
            List<String> namedKeys,
            boolean hasDefaultValue,
            T defaultValue
        ) {
            this.name = name;
            this.type = type;
            this.positional = positional;
            this.namedKeys = namedKeys;
            this.hasDefaultValue = hasDefaultValue;
            this.defaultValue = defaultValue;
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
