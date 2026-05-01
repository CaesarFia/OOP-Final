# Command System

Handles creation of command structures and multi-argument parsing.

## Development Notes

built around a Command class that holds the structure definition separately from the parse result. the idea is you define the command once and reuse it, and each call to parse() gives back a fresh ParsedArguments.

named args get resolved first, then positional ones fill in what's left in declaration order. felt like the natural way to handle mixed commands.

errors throw as CommandException so they don't leak out as raw RuntimeExceptions. the scenarios wrap those in RuntimeException since that's what the test harness expects.

added constValue for flag-style named args (like --case-insensitive with no value). also added basic subcommand support for dispatch since you can't really do that cleanly with just positional/named args.

## MVP Design Analysis

### Individual Review (Command Lead)

good decisions:
- constValue ended up being a clean solution for flag-style args. instead of making a special boolean flag type, any named parameter can define a value to use when the flag is present with no argument. kept the type system simpler
- subcommands were added as a first-class thing in the builder rather than hacking it together in scenario code. dispatch needed different argument shapes depending on the subcommand so there wasn't really another way to do it cleanly
- error handling is consistent: CommandException for anything wrong at the command/parse level, ArgumentException for bad values at the argument level. scenarios catch CommandException and wrap it in RuntimeException since that's what the test harness expects. nothing throws raw RuntimeException from library code

less good:
- alias support works but it creates a weird situation where ParsedArguments has to preserve the key the user actually typed ("i" vs "case-insensitive") for the output map, while also letting you do typed gets by canonical name. it works but it means ParsedArguments is doing two slightly different things
- subcommand support is pretty minimal, it handles one level deep and that's it. anything more complex would probably need a bigger redesign

### Individual Review (Argument Lead)

### Team Review

## PoC Design Analysis

### Individual Review (Command Lead)

good decisions:
- keeping Command (the definition) separate from ParsedArguments (the result) made things cleaner. you define the shape once and parse as many times as you want without any state carrying over
- validating the command structure at build time instead of parse time means you catch mistakes like duplicate named keys or a parameter that's neither positional nor named before anything even runs

less good:
- the builder chains get hard to read when a parameter has positional + named + default + constValue all on it, not sure there's a cleaner way but it's a lot to look at
- ParsedArguments has to track both the display key (what the user actually typed) and the canonical name for typed getters, which makes it do more than it probably should

### Individual Review (Argument Lead)
- Good: Typed getters (`getInt`, `getString`, `getBoolean`, etc.) are a strong command-side design choice because they prevent consumers from treating parsed results as an unchecked map of objects.
- Bad: Default handling is currently eager and value-based only, which limits future cases where a default should depend on runtime context or another argument.

### Team Review
- Current design disagreement: We do not fully agree on whether future subcommands and richer defaults should extend `Command` directly or live in a higher-level command-tree abstraction.
- Current agreed concern: Alias-preserving output helps the current scenarios and tests, but we do not yet have a final design for how aliases, canonical names, and help/usage text should coexist without confusion.
