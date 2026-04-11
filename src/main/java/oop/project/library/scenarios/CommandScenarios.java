package oop.project.library.scenarios;

import java.util.Map;

public final class CommandScenarios {

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        // two ints, positional
        // need left and right
        // return as map
        throw new UnsupportedOperationException("TODO (PoC)");
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        // named args, --left --right
        // doubles not ints
        // same map return
        throw new UnsupportedOperationException("TODO (PoC)");
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        // optional message arg
        // has a default value
        throw new UnsupportedOperationException("TODO (MVP)");
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        // term + optional flag
        // --case-insensitive is boolean
        throw new UnsupportedOperationException("TODO (MVP)");
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        // subcommands: static, dynamic
        // static needs int, dynamic any string
        throw new UnsupportedOperationException("TODO (MVP)");
    }

}
