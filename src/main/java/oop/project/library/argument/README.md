# Argument System

Handles parsing a single String input value into typed data.

## Development Notes

- Centralized parsing into `ArgumentType<T>` so type conversion and validation can be reused by both positional and named command arguments.
- Added validator hooks instead of hard-coding range and choice checks into each scenario.
- Kept validation attached to the type abstraction so a validated type can be reused in multiple commands without duplicating rules.
- Added enum and regex support as general-purpose features rather than solving those cases in scenario code.
- Kept `ArgumentException` separate from command-level errors so invalid value parsing and invalid command structure remain different failure modes.

## PoC Design Analysis

### Individual Review (Argument Lead)
- Good: Using one polymorphic `ArgumentType<T>` abstraction for built-in, custom, and enum-backed parsing avoids hardcoding scenario-specific data types into the library.
- Good: Treating validation as a composable decorator on top of parsing makes range, choice, and regex checks reusable across multiple commands.
- Bad: Validation currently lives on the same abstraction as parsing, which keeps the API small but also couples two responsibilities that could grow at different rates.
- Bad: The library exposes typed parsing well, but it still has limited metadata around each argument type, which would make future help text or richer diagnostics harder to add cleanly.

### Individual Review (Command Lead)
- Good: The command system depends on `ArgumentType<T>` through a narrow parsing/validation contract instead of knowing details about specific value types.
- Bad: Alias-preserving output in parsed command results means the command layer now has some presentation concerns that partially leak into how argument names appear externally.

### Team Review
- Current design disagreement: We do not fully agree on whether validation belongs directly on `ArgumentType<T>` or should move into a separate validator object/layer once the library grows further.
- Current agreed concern: Custom parsing is flexible enough for the MVP, but we do not yet have a strong design for richer error reporting that distinguishes parse failure, validation failure, and usage hints cleanly.
