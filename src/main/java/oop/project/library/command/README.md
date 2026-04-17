# Command System

Handles creation of command structures and multi-argument parsing.

## Development Notes

- Commands are built from typed parameters that can be positional, named, or both.
- Parsing resolves named arguments first and then consumes remaining positionals in declaration order, which keeps mixed commands predictable.
- `ParsedArguments` exposes typed getters so downstream code does not have to cast values out of a raw `Map<String, Object>`.
- Command creation validates key structure constraints up front, such as duplicate named keys and parameters with no usable binding mode.
- `ParsedArguments` stores command state separately from `Command`, which keeps reusable command definitions distinct from one parse result.
- Named alias handling preserves the key used in input for scenario reporting, while canonical typed getters still work against the declared parameter name.
- Parameters can define a `constValue(...)` for no-value named usage, which keeps flag-like behavior out of the type system.
- Added minimal subcommand support so different argument structures can be modeled in the library instead of being hardcoded in scenario methods.

## PoC Design Analysis

### Individual Review (Command Lead)
- Good: Representing each command as a reusable definition object and each parse as a separate `ParsedArguments` object gives a clean separation between structure and state.
- Good: Enforcing command-shape rules during builder registration centralizes structural validation instead of relying on every scenario author to avoid invalid command definitions manually.
- Bad: The builder API is flexible, but it is still fairly stateful and order-sensitive, which makes misuse possible if future features add more parameter options.
- Bad: Alias-preserving display keys and canonical typed getters now serve two audiences at once, which makes `ParsedArguments` more complex than a purely canonical result object.

### Individual Review (Argument Lead)
- Good: Typed getters (`getInt`, `getString`, `getBoolean`, etc.) are a strong command-side design choice because they prevent consumers from treating parsed results as an unchecked map of objects.
- Bad: Default handling is currently eager and value-based only, which limits future cases where a default should depend on runtime context or another argument.

### Team Review
- Current design disagreement: We do not fully agree on whether future subcommands and richer defaults should extend `Command` directly or live in a higher-level command-tree abstraction.
- Current agreed concern: Alias-preserving output helps the current scenarios and tests, but we do not yet have a final design for how aliases, canonical names, and help/usage text should coexist without confusion.
