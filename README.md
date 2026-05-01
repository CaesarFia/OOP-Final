# OOP Project Library

A library for building commands and parsing typed arguments from string input. The code is split into three layers:

- `oop.project.library.input`: tokenizes raw command text into positional and named arguments
- `oop.project.library.argument`: parses a single raw value into a typed result and applies reusable validators
- `oop.project.library.command`: defines full command structures, resolves arguments, and exposes typed extraction

## Beginner's Guide

### Argument System

The Argument System is responsible for taking a raw string and converting it into a typed value. The main thing you work with is `ArgumentType<T>`.

Built-in types:
- `ArgumentType.integer()` parses to `int`
- `ArgumentType.dbl()` parses to `double`
- `ArgumentType.bool()` parses to `boolean`, accepts `true`/`false`
- `ArgumentType.string()` passes the value through as a `String`

You can also add validation on top of a type:

```java
// only accept integers between 1 and 10
ArgumentType.integer().validate(Validators.range(1, 10))

// only accept specific string choices
ArgumentType.string().validate(Validators.choices(List.of("easy", "normal", "hard")))
```

If parsing or validation fails, an `ArgumentException` is thrown with a message describing what went wrong.

### Command System

The Command System is how you define the structure of a command: which arguments it accepts, whether they're positional or named, and what types they expect. Once you define a command you can call `parse()` on it with a raw string and get back a `ParsedArguments` object.

#### Creating a basic command

```java
Command greet = Command.builder("greet")
    .addParameter("name", ArgumentType.string()).positional().add()
    .build();
```

Then to use it:

```java
ParsedArguments result = greet.parse("Alice");
String name = result.getString("name"); // "Alice"
```

#### Positional arguments

Positional arguments are consumed in the order they're declared. They don't need a flag.

```java
Command add = Command.builder("add")
    .addParameter("left", ArgumentType.integer()).positional().add()
    .addParameter("right", ArgumentType.integer()).positional().add()
    .build();

ParsedArguments result = add.parse("3 5");
int left = result.getInt("left");   // 3
int right = result.getInt("right"); // 5
```

#### Named arguments

Named arguments use `--name value` syntax in the input string. You declare them with `.named("key")`.

```java
Command divide = Command.builder("divide")
    .addParameter("left", ArgumentType.dbl()).named("left").add()
    .addParameter("right", ArgumentType.dbl()).named("right").add()
    .build();

ParsedArguments result = divide.parse("--left 10.0 --right 4.0");
double left = result.getDouble("left");   // 10.0
double right = result.getDouble("right"); // 4.0
```

Named arguments can be provided in any order. If a required named argument is missing, a `CommandException` is thrown.

#### Default values

You can give a parameter a default value so it becomes optional:

```java
Command echo = Command.builder("echo")
    .addParameter("message", ArgumentType.string())
        .positional().named("message")
        .defaultValue("echo,echo,echo...")
        .add()
    .build();

ParsedArguments r1 = echo.parse("");       // message = "echo,echo,echo..."
ParsedArguments r2 = echo.parse("hello");  // message = "hello"
```

#### Extracting parsed values

`ParsedArguments` has typed getters so you don't have to cast anything yourself:

```java
result.getInt("name")
result.getDouble("name")
result.getString("name")
result.getBoolean("name")
result.get("name", SomeClass.class) // generic version
```

If the argument doesn't exist or is the wrong type, a `CommandException` is thrown.

## Error Model

The library uses checked exceptions for the core parsing pipeline:

- `InputException`: invalid raw command text such as duplicate named keys, unterminated quotes, or a missing value after a double flag
- `ArgumentException`: invalid individual values or failed validation
- `CommandException`: invalid command usage such as missing required arguments, unknown named keys, or subcommand mismatches

The provided `scenarios` package wraps those checked failures in `RuntimeException` because the course scenario harness expects scenario methods to fail from within the scenario layer.

## Feature Showcase

The required MVP already supports typed positional and named arguments, defaults, custom parsing, and typed extraction. The showcase features in this submission are subcommand-aware parsing with typed nested results and const-value flags for boolean-style switches.

### Showcase 1: Constant-value flags

One thing that is awkward in `argparse4j` is handling boolean flags where you do not want the user to type `--verbose true`; you just want `--verbose` by itself to mean true when present and false when absent.

In this library you can use `.constValue()` on any named parameter. When the argument appears in the input but has no value following it, it uses the const value instead of trying to parse one.

```java
Command search = Command.builder("search")
    .addParameter("term", ArgumentType.string()).positional().add()
    .addParameter("case-insensitive", ArgumentType.bool())
        .named("case-insensitive")
        .constValue(true)
        .defaultValue(false)
        .add()
    .build();

search.parse("apple");                          // case-insensitive = false
search.parse("apple --case-insensitive");       // case-insensitive = true
search.parse("apple --case-insensitive true");  // case-insensitive = true
```

This keeps flag behavior out of the type system. `ArgumentType.bool()` still behaves normally when a value is provided, but you still get ergonomic no-value flag usage.

### Showcase 2: Subcommands with different typed shapes

The `dispatch` scenario demonstrates one command name branching into different typed argument structures:

```text
dispatch static 1
=> {type=static, value=1}

dispatch dynamic one
=> {type=dynamic, value=one}
```

`static` and `dynamic` are parsed as true subcommands, not as ad hoc strings checked later in scenario code. Each branch gets its own command definition and its own typed extraction path.

## Scenario Coverage

Scenario examples implemented in `src/main/java/oop/project/library/scenarios` include:

- `difficulty easy`
  Demonstrates enum parsing with case-insensitive matching.
- `date 2024-10-23`
  Demonstrates `ArgumentType.custom(...)` with typed extraction as `LocalDate`.
- `search ApPlE --i true`
  Demonstrates alias support while preserving the actual alias used in the scenario output map.
- `dispatch static 1`
  Demonstrates subcommand parsing and nested typed results.

Focused tests for the library API live in:

- `src/test/java/oop/project/library/argument/ArgumentLibraryTests.java`
- `src/test/java/oop/project/library/command/CommandLibraryTests.java`
- `src/test/java/oop/project/library/input/InputTests.java`

## Running

```bash
bash gradlew test
```

Additional design-review notes for the argument and command subsystems are in:

- `src/main/java/oop/project/library/argument/README.md`
- `src/main/java/oop/project/library/command/README.md`
