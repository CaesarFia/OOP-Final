package oop.project.library.argument;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArgumentLibraryTests {

    private enum Difficulty {
        PEACEFUL,
        EASY,
        NORMAL,
        HARD
    }

    @Test
    void enumerationParsesIgnoringCase() throws ArgumentException {
        var type = ArgumentType.enumeration(Difficulty.class);
        Assertions.assertEquals(Difficulty.EASY, type.parse("difficulty", "easy"));
        Assertions.assertEquals(Difficulty.HARD, type.parse("difficulty", "HARD"));
    }

    @Test
    void regexValidatorAcceptsAndRejectsExpectedValues() throws ArgumentException {
        var type = ArgumentType.string().validate(Validators.regex("[A-Z]+-[IV]+"));
        Assertions.assertEquals("ABC-IV", type.parse("roman", "ABC-IV"));
        Assertions.assertThrows(ArgumentException.class, () -> type.parse("roman", "abc-iv"));
    }

}
