@echo off
chcp 65001 >nul
cd /d "%~dp0"
java -Dfile.encoding=UTF-8 --module-path "libs\javafx\win" --add-modules javafx.controls -jar PlayfairCipher.jar --gui
pause
