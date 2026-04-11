package oop.project.library.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public BasicArgs parseBasicArgs() {
        var positional = new ArrayList<String>();
        var named = new LinkedHashMap<String, String>();

        while (true) {
            switch (parseValue().orElse(null)) {
                case null -> {
                    return new BasicArgs(List.copyOf(positional), Map.copyOf(named));
                }
                case Value.Literal(String value) -> positional.add(value);
                case Value.QuotedString(String value) -> positional.add(value);
                case Value.SingleFlag(String name) -> putNamed(named, name, "");
                case Value.DoubleFlag(String name) -> {
                    var checkpoint = index;
                    switch (parseValue().orElse(null)) {
                        case Value.Literal(String value) -> putNamed(named, name, value);
                        case Value.QuotedString(String value) -> putNamed(named, name, value);
                        case null -> {
                            index = checkpoint;
                            putNamed(named, name, "");
                        }
                        default -> {
                            index = checkpoint;
                            putNamed(named, name, "");
                        }
                    }
                }
            }
        }
    }

    public Optional<Value> parseValue() {
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

    private Optional<Value> parseQuotedString() {
        var start = index;
        do {
            index++;
        } while (index < chars.length && chars[index] != '"');

        if (index >= chars.length) {
            throw new RuntimeException("Unterminated quoted string @ index " + start + ".");
        }

        var value = new String(chars, start + 1, index - start - 1);
        index++;

        if (index < chars.length && chars[index] != ' ') {
            throw new RuntimeException("Invalid quote within literal @ index " + index + ".");
        }

        return Optional.of(new Value.QuotedString(value));
    }

    private Optional<Value> parseLiteralOrFlag() {
        var start = index;
        do {
            index++;
        } while (index < chars.length && chars[index] != ' ' && chars[index] != '"');

        if (index < chars.length && chars[index] == '"') {
            throw new RuntimeException("Invalid quote within literal @ index " + index + ".");
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

    private static void putNamed(Map<String, String> named, String name, String value) {
        if (named.putIfAbsent(name, value) != null) {
            throw new RuntimeException("Duplicate named argument " + name + ".");
        }
    }

}
