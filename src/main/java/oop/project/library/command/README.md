# Command System

Handles creation of command structures and multi-argument parsing.

## commands

- mul: two positional ints
- div: two named doubles (--left, --right)
- echo: one optional string, default exists

- search: string + optional bool flag
- dispatch: subcommand then value

## notes

using Basicargs from input package to hold parsed stuff
argument system handles the actual parsing, we just use the result

## Development Notes

for mul i just split on whitespace and parsed each token as int. pretty straightforward since its positional. for div i looped through tokens looking for --left and --right flags which is basically how argparse does it. tried to guard against negative numbers being confused with flags but the check only works for integers, so negative decimals might be an issue (will fix later).

## PoC Design Analysis

### Individual Review (Command Lead)

good decisions:
- splitting positional vs named arg handling early. mul and div feel pretty different so treating them differently makes sense, didnt try to force one solution for both
- throwing RuntimeException with a message for bad input instead of returning null or something, makes it easier to see whats wrong

less good:
- the negative number check in div is kind of hacky, using Integer.parseInt just to validate that a value isnt a flag is not great. probably a cleaner way to do this
- a lot of repeated code between --left and --right parsing in div, should probably be a helper at some point

upcoming:
- still need echo, search, dispatch for mvp. echo seems simple, dispatch will be more complex since it has subcommands

### Individual Review (Argument Lead)

### Team Review
