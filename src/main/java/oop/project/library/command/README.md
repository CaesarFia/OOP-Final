# Command System

Handles creation of command structures and multi-argument parsing.

## commands

- mul: two positional ints
- div: two named doubles (--left, --right)
- echo: one optional string, default exists
- search: string + optional bool flag
- dispatch: subcommand then value

## notes

using BasicArgs from input package to hold parsed stuff
argument system handles the actual parsing, we just use the result

## Development Notes

TODO: Keep a running log of design decisions, tradeoffs, and other observations.

## PoC Design Analysis

### Individual Review (Command Lead)

### Individual Review (Argument Lead)

### Team Review
