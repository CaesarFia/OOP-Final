package oop.project.library.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tokenizes raw command text into literals, quoted strings, and named flags.
 */
public final class Input {

    public sealed interface Value {
        record Literal(String value) implements Value {}
        record QuotedString(String value) implements Value {}
        record SingleFlag(String name) implements Value {}
        record DoubleFlag(String name) implements Value {}
    }

    private final char[] chars;
    private int index = 0;

    public Input(String input) {
        chars = input.toCharArray();
    }

    /**
     * Parses the remaining input into positional and named argument collections.
     *
     * @throws InputException if the raw text is syntactically invalid
     */
    public BasicArgs parseBasicArgs() throws InputException {
        var args = new BasicArgs(new ArrayList<>(), new LinkedHashMap<>());
        while (true) {
            switch (parseValue().orElse(null)) {
                case null -> { return args; }
                case Value.Literal(String value) -> args.positional().add(value);
                case Value.QuotedString(String value) -> args.positional().add(value);
                case Value.SingleFlag(String name) -> putNamed(args.named(), name, "");
                case Value.DoubleFlag(String name) -> {
                    switch (parseValue().orElse(null)) {
                        case Value.Literal(String value) -> putNamed(args.named(), name, value);
                        case Value.QuotedString(String value) -> putNamed(args.named(), name, value);
                        case null, default -> throw new InputException(
                            "Double flag --" + name + " is missing a value @ index " + index + "."
                        );
                    }
                }
            }
        }
    }

    /**
     * Parses the next raw token from the input, if present.
     */
    public Optional<Value> parseValue() throws InputException {
        while (index < chars.length && chars[index] == ' ') {
            index++;
        }

        if (index >= chars.length) {
            return Optional.empty();
        }

        if (chars[index] == '"') {
            return parseQuotedString();
        }

        return parseLiteralOrFlag();
    }

    public int checkpoint() {
        return index;
    }

    public void restore(int checkpoint) {
        index = checkpoint;
    }

    private Optional<Value> parseQuotedString() throws InputException {
        var start = index;
        do {
            index++;
        } while (index < chars.length && chars[index] != '"');

        if (index >= chars.length) {
            throw new InputException("Unterminated quoted string @ index " + start + ".");
        }

        var value = new String(chars, start + 1, index - start - 1);
        index++;

        if (index < chars.length && chars[index] != ' ') {
            throw new InputException("Invalid quote within literal @ index " + index + ".");
        }

        return Optional.of(new Value.QuotedString(value));
    }

    private Optional<Value> parseLiteralOrFlag() throws InputException {
        var start = index;
        do {
            index++;
        } while (index < chars.length && chars[index] != ' ' && chars[index] != '"');

        if (index < chars.length && chars[index] == '"') {
            throw new InputException("Invalid quote within literal @ index " + index + ".");
        }

        var value = new String(chars, start, index - start);
        if (value.startsWith("--") && value.length() > 2 && Character.isLetter(value.charAt(2))) {
            return Optional.of(new Value.DoubleFlag(value.substring(2)));
        }
        if (value.startsWith("-") && value.length() > 1 && Character.isLetter(value.charAt(1))) {
            return Optional.of(new Value.SingleFlag(value.substring(1)));
        }
        return Optional.of(new Value.Literal(value));
    }

    private static void putNamed(Map<String, String> namedArguments, String name, String value) throws InputException {
        if (namedArguments.putIfAbsent(name, value) != null) {
            throw new InputException("Duplicate named argument '" + name + "'.");
        }
    }
}
