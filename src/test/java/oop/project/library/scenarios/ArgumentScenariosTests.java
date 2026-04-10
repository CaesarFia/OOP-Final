package oop.project.library.scenarios;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import oop.project.library.input.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

class ArgumentScenariosTests {

    @ParameterizedTest
    @MethodSource
    public void testAdd(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testAdd() {
        return Stream.of(
            Arguments.of("Positives", """
                add 1 2
                """, Map.of("left", 1, "right", 2)),
            Arguments.of("Negatives", """
                add -1 -2
                """, Map.of("left", -1, "right", -2)),
            Arguments.of("Zeros", """
                add 0 0
                """, Map.of("left", 0, "right", 0)),
            Arguments.of("Non-Numeric Left", """
                add one 2
                """, null),
            Arguments.of("Non-Numeric Right", """
                add 1 two
                """, null),
            Arguments.of("Non-Integer Left", """
                add 1.0 2
                """, null),
            Arguments.of("Non-Integer Right", """
                add 1 2.0
                """, null),
            Arguments.of("Missing Argument", """
                add 1
                """, null),
            Arguments.of("Extraneous Argument", """
                add 1 2 3
                """, null)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testSub(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testSub() {
        return Stream.of(
            Arguments.of("Positives", """
                sub 1.0 2.0
                """, Map.of("left", 1.0, "right", 2.0)),
            Arguments.of("Negative Integer", """
                sub 1.0 -2
                """, Map.of("left", 1.0, "right", -2.0)),
            Arguments.of("Negative Decimal (FIXED BUG)", """
                sub 1.0 -2.0
                """, Map.of("left", 1.0, "right", -2.0))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFizzbuzz(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testFizzbuzz() {
        return Stream.of(
            Arguments.of("Valid", """
                fizzbuzz 15
                """, Map.of("number", 15)),
            Arguments.of("Zero", """
                fizzbuzz 0
                """, null)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testDifficulty(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testDifficulty() {
        return Stream.of(
            Arguments.of("Easy", """
                difficulty easy
                """, Map.of("difficulty", "easy")),
            Arguments.of("Hardcore", """
                difficulty hardcore
                """, null)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testDate(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testDate() {
        return Stream.of(
            Arguments.of("Valid", """
                date 2024-10-23
                """, Map.of("date", LocalDate.of(2024, 10, 23))),
            Arguments.of("Invalid Month", """
                date 2024-23-10
                """, null),
            Arguments.of("Leap Year", """
                date 2024-02-29
                """, Map.of("date", LocalDate.of(2024, 2, 29))),
            Arguments.of("Invalid", """
                date tuesday
                """, null)
        );
    }

    private static void test(String command, Map<String, Object> expected) {
        try {
            var result = Scenarios.parse(command.stripTrailing()); //trailing newline
            Assertions.assertEquals(expected, result);
        } catch (RuntimeException e) {
            if (!e.getStackTrace()[0].getClassName().startsWith("oop.project.library.scenarios")) {
                Assertions.fail("Unexpected exception, expected an exception thrown from within a Scenario.", e);
            } else if (expected != null || e instanceof UnsupportedOperationException) {
                Assertions.fail(e.getCause() != null ? e.getCause() : e);
            }
        }
    }

}
