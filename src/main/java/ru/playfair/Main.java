package ru.playfair;

/** Единая точка входа консольного и графического режимов. */
public final class Main {
    /** Запрещает создание экземпляров. */
    private Main() {}

    /**
     * Запускает консоль или JavaFX по флагу --gui.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--gui")) {
            ru.playfair.gui.PlayfairApp.main(args);
        } else if (args.length == 0) {
            new ConsoleUI().run();
        } else {
            System.err.println("Использование: java -jar PlayfairCipher.jar [--gui]");
        }
    }
}
