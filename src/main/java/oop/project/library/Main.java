package oop.project.library;

import oop.project.library.scenarios.Scenarios;

import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        while (true) {
            var input = scanner.nextLine();
            try {
                var parsedArgs = Scenarios.parse(input);
                System.out.println(parsedArgs);
            } catch (Throwable t) {
                System.out.println("Unexpected " + t.getClass().getSimpleName() + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

}
