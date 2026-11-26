# PlayfairCipher (Java 17 + Gradle)

Симметричный шифр Плейфера для алфавита из 256 символов (матрица 16×16).

## Требования
- JDK 17+
- Gradle 7+ (или используйте wrapper `./gradlew`)

## Автор и цель
- Кокорин Артемий Александрович
- Учебный процесс 2026 года
- Преподаватель Цветков А.Ю.

## Порядок работы
Выберите шифрование или расшифрование, существующий файл, ключ от 3 символов и НОВЫЙ путь для результата. 

Существующие файлы не перезаписываются. 

Ключ чувствителен к регистру и пробелам. Запомните точную фразу. 

Пустые файлы поддерживаются. Максимальный размер исходных данных - 64 МБ; при расшифровании дополнительно допускаются 20 байтов заголовка.


В консоли слово «отмена» возвращает в меню. Команда 3 завершает работу. В GUI сначала выберите файл, введите ключ и нажмите нужную кнопку, затем укажите новый путь результата.

## Сборка
./gradlew clean build

Из каталога project:
  
 -- macOS/Linux: ./gradlew clean test jar javadoc
  
 -- Windows: gradlew.bat clean test jar javadoc

Проверено на JDK 17.0.17 и Gradle 8.14.3. При первом вызове Wrapper скачает Gradle; библиотеки уже включены в project/libs. Для повторной сборки без сети после загрузки Gradle добавьте --offline.

Результат сборки: project/build/libs/PlayfairCipher.jar.

Для обновления готовой программы в корне, пожалуйста, скопируйте туда новый JAR после сборки.

## Запуск
./gradlew run [ИЛИ] java -jar build/libs/PlayfairCipher.jar

Консоль: java -jar PlayfairCipher.jar

Windows: run-console.bat или run-gui.bat.

macOS: run-console.command или run-gui.command. Если нет права запуска:
  
--  chmod +x run-console.command run-gui.command
  
--  ./run-gui.command

Linux x64: sh run-gui.command

В графическом режиме Java должна соответствовать архитектуре ОС.

Если вы уже в директории с проектом, то команды для терминала:
-- run-gui.bat
-- java -Dfile.encoding=UTF-8 --module-path "project\libs\javafx\win" --add-modules javafx.controls -jar PlayfairCipher.jar --gui

-- run-console.bat
-- java -Dfile.encoding=UTF-8 --module-path "project\libs\javafx\win" --add-modules javafx.controls -jar PlayfairCipher.jar

## Функции
- Шифрование / расшифрование файлов любого типа
- Многоуровневая защита от некорректного ввода
- Кроссплатформенность (JVM)
