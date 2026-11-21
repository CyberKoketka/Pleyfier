package ru.playfair.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.playfair.FileService;

import java.io.File;

/** Минимальный JavaFX-интерфейс, использующий общую файловую службу. */
public final class PlayfairApp extends Application {
    /** Логгер событий интерфейса. */
    private static final Logger LOGGER = LogManager.getLogger(PlayfairApp.class);

    /** Выбранный исходный файл. */
    private File inputFile;

    /** Поле ключа. */
    private final PasswordField keyField = new PasswordField();

    /** Область результатов и ошибок. */
    private final TextArea messages = new TextArea();

    /** Контейнер элементов управления. */
    private final GridPane grid = new GridPane();

    /** Окно для диалогов выбора файлов. */
    private Stage window;

    /** Создаёт графический интерфейс. */
    public PlayfairApp() {}

    /**
     * Создаёт окно и привязывает обработчики кнопок.
     *
     * @param stage главное окно JavaFX
     */
    @Override
    public void start(Stage stage) {
        window = stage;
        messages.setEditable(false);
        messages.setWrapText(true);
        messages.setPrefRowCount(10);
        keyField.setPromptText("Не менее 3 символов");
        Button choose = new Button("Выбрать файл");
        choose.setOnAction(event -> chooseFile());
        Button encrypt = new Button("Зашифровать");
        encrypt.setOnAction(event -> process(true));
        Button decrypt = new Button("Расшифровать");
        decrypt.setOnAction(event -> process(false));
        grid.setPadding(new Insets(16));
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(new Label("Ключ:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(choose, 0, 1, 2, 1);
        grid.add(encrypt, 0, 2);
        grid.add(decrypt, 1, 2);
        grid.add(messages, 0, 3, 2, 1);
        stage.setScene(new Scene(grid, 620, 390));
        stage.setTitle("PlayfairCipher 2.0 — учебный шифр");
        stage.show();
    }

    /** Открывает диалог выбора входного файла. */
    private void chooseFile() {
        File selected = new FileChooser().showOpenDialog(window);
        if (selected != null) {
            inputFile = selected;
            log("Выбран файл: " + selected.getAbsolutePath());
        }
    }

    /**
     * Выбирает новый выходной файл и запускает фоновую операцию.
     *
     * @param encrypt true для шифрования
     */
    private void process(boolean encrypt) {
        if (inputFile == null) {
            log("Ошибка: файл не выбран");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить в новый файл");
        chooser.setInitialFileName(inputFile.getName() + (encrypt ? ".pfc" : ".restored"));
        File target = chooser.showSaveDialog(window);
        if (target == null) return;
        File source = inputFile;
        String key = keyField.getText();
        Task<Long> task =
                new Task<>() {
                    /**
                     * Выполняет файловую операцию вне потока интерфейса.
                     *
                     * @return размер результата
                     * @throws Exception при ошибке обработки
                     */
                    @Override
                    protected Long call() throws Exception {
                        return new FileService()
                                .process(source.toPath(), target.toPath(), key, encrypt);
                    }
                };
        task.setOnSucceeded(
                event -> {
                    grid.setDisable(false);
                    log("Готово: " + task.getValue() + " байт. " + target.getAbsolutePath());
                });
        task.setOnFailed(
                event -> {
                    grid.setDisable(false);
                    log("Ошибка: " + task.getException().getMessage());
                });
        grid.setDisable(true);
        Thread worker = new Thread(task, "file-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Добавляет сообщение в окно и журнал, не раскрывая ключ.
     *
     * @param message текст события
     */
    private void log(String message) {
        messages.appendText(message + "\n");
        LOGGER.info(message);
    }

    /**
     * Запускает JavaFX.
     *
     * @param args аргументы запуска
     */
    public static void main(String[] args) {
        launch(args);
    }
}
