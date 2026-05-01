package oop.project.library.input;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InputTests {

    @Test
    void duplicateNamedKeysAreRejectedDuringTokenization() {
        var input = new Input(" --term apple --term banana");
        Assertions.assertThrows(InputException.class, input::parseBasicArgs);
    }

    @Test
    void missingDoubleFlagValueIsRejected() {
        var input = new Input(" --left");
        Assertions.assertThrows(InputException.class, input::parseBasicArgs);
    }
}
