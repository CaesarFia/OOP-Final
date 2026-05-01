# OOP Project Library

A library for building commands and parsing typed arguments from string input. There are two main systems that work together: the Argument System handles converting raw string values into typed data, and the Command System handles the structure of commands and how arguments get bound to them.

---

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
ArgumentType.string().validate(Validators.choices("easy", "normal", "hard"))
```

If parsing or validation fails, an `ArgumentException` is thrown with a message describing what went wrong.

---

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

---

## Feature Showcase

### Command System: Constant-value flags

One thing that's kind of annoying in argparse4j is handling boolean flags where you don't want the user to type `--verbose true`, you just want `--verbose` by itself to mean true when present and false when not. In argparse4j you need a separate `store_true` action type to do this cleanly.

In this library you can use `.constValue()` on any named parameter. When the argument appears in the input but has no value following it, it uses the const value instead of trying to parse one.

```java
Command search = Command.builder("search")
    .addParameter("term", ArgumentType.string()).positional().add()
    .addParameter("case-insensitive", ArgumentType.bool())
        .named("case-insensitive")
        .constValue(true)    // used when flag is present with no value
        .defaultValue(false) // used when flag is absent entirely
        .add()
    .build();

// all three of these work:
search.parse("apple");                          // case-insensitive = false
search.parse("apple --case-insensitive");       // case-insensitive = true
search.parse("apple --case-insensitive true");  // case-insensitive = true
```

This keeps the flag behavior out of the type system. `ArgumentType.bool()` still does what it normally does when a value is provided, but you get the convenience of value-less flag usage without needing a special action type.
