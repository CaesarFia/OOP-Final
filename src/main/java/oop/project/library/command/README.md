# Command System

Handles creation of command structures and multi-argument parsing.

## Development Notes

- Commands are built from typed parameters that can be positional, named, or both.
- Parsing resolves named arguments first and then consumes remaining positionals in declaration order, which keeps mixed commands predictable.
- `ParsedArguments` exposes typed getters so downstream code does not have to cast values out of a raw `Map<String, Object>`.

## PoC Design Analysis

### Individual Review (Command Lead)
- Good: One parameter can be both positional and named, which handled cases like `echo --message value` without duplicating command logic.
- Good: Extraneous positional and unknown named arguments are rejected centrally instead of in each scenario.
- Less-good: Repeated arguments and list-valued arguments are not supported yet, so future variadic features will need an API extension.
- Less-good: `dispatch` still uses scenario-side logic for its type-dependent second argument because subcommands are out of scope for this checkpoint.

### Individual Review (Argument Lead)
- Good: The typed getter API (`getInt`, `getString`, etc.) makes the command results safer to consume in normal Java code.
- Less-good: Default handling is currently value-based only; lazy defaults or derived defaults may be needed later.

### Team Review
- We still need to decide whether future defaults and subcommands belong directly in `Command` or in a higher-level builder abstraction.
- Alias handling is sufficient for checkpoint 2, but we have not finalized how it should interact with richer future command trees.
