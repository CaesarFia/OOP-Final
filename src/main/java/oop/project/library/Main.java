package oop.project.library;

import oop.project.library.scenarios.Scenarios;

import java.util.Scanner;

public final class Main {

    static void main() {
        var scanner = new Scanner(System.in);
        while (true) {
            var input = scanner.nextLine();
            try {
                var args = Scenarios.parse(input);
                System.out.println(args);
            } catch (Throwable t) {
                System.out.println("Unexpected " + t.getClass().getSimpleName() + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

}
