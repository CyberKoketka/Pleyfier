package ru.playfair;

/**
 * Точка входа приложения. Делегирует всю работу ConsoleUI.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        new ConsoleUI().run();
    }
}