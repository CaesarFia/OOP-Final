# Argument System

Handles parsing a single String input value into typed data.

## Development Notes

- Centralized parsing into `ArgumentType<T>` so type conversion and validation can be reused by both positional and named command arguments.
- Added validator hooks instead of hard-coding range and choice checks into each scenario.
- Implemented custom types through `ArgumentType.custom(...)`, which lets types like `LocalDate` plug into the same path as the built-in primitives.

## PoC Design Analysis

### Individual Review (Argument Lead)
- Good: Built-in types and custom types share the same API, so the date scenario no longer needs a one-off parsing branch.
- Good: Validation composes with parsing cleanly, which kept `fizzbuzz` and `difficulty` short and readable.
- Less-good: Validator helpers currently cover checkpoint use cases only; enum and regex helpers still need to be added.
- Less-good: Error messages are aimed more at debugging than polished CLI UX.

### Individual Review (Command Lead)
- Good: The argument package stays decoupled from command structure, which made it easy to test parsing and validation logic independently.
- Less-good: Argument definitions do not yet include help text or richer metadata that would matter for usage generation later.

### Team Review
- We agree on keeping parsing and validation separate concerns, but we still need to decide whether later constraints should remain on `ArgumentType<T>` or move into their own layer.
- Enum support and regex validation should fit the current shape, but the final public API for those additions is still open.
