package oop.project.library.scenarios;

import java.util.HashMap;
import java.util.Map;

public final class CommandScenarios {

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        // two ints, positional
        // need left and right
        // return as map
        String[] tokens = arguments.trim().split("\\s+");
        if (tokens.length != 2) {
            throw new RuntimeException("expected exactly 2 arguments");
        }
        int left, right;
        try {
            left = Integer.parseInt(tokens[0]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("left must be an integer");
        }
        try {
            right = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("right must be an integer");
        }
        var res = new HashMap<String, Object>();
        res.put("left", left);
        res.put("right", right);
        return res;
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        // named args --left --right
        // doubles not ints
        // same map return
        String[] tokens = arguments.trim().split("\\s+");
        double left = 0, right = 0;
        boolean gotLeft = false, gotRight = false;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("--left")) {
                String val = tokens[i + 1];
                // make sure its not another flag
                if (val.startsWith("-")) {
                    try {
                        Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("bad value for --left: " + val);
                    }
                }
                left = Double.parseDouble(val);
                gotLeft = true;
                i++;
            } else if (tokens[i].equals("--right")) {
                String val = tokens[i + 1];
                if (val.startsWith("-")) {
                    try {
                        Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("bad value for --right: " + val);
                    }
                }
                right = Double.parseDouble(val);
                gotRight = true;
                i++;
            } else {
                throw new RuntimeException("unknown argument: " + tokens[i]);
            }
        }

        if (!gotLeft || !gotRight) {
            throw new RuntimeException("missing --left or --right");
        }
        var result = new HashMap<String, Object>();
        result.put("left", left);
        result.put("right", right);
        return result;
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        // optional message argu
        // has a default value
        throw new UnsupportedOperationException("TODO (MVP)");
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        // term + optional flag
        // --case-insensitive is boolean
        throw new UnsupportedOperationException("TODO (MVP)");
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        // subcommands static dynamic
        // static needs int, dynamic any string
        throw new UnsupportedOperationException("TODO (MVP)");
    }

}
