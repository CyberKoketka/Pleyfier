#!/bin/sh
cd "$(dirname "$0")" || exit 1
case "$(uname -s)" in
  Darwin) case "$(uname -m)" in arm64) platform=mac-aarch64 ;; *) platform=mac ;; esac ;;
  Linux) platform=linux ;;
  *) echo "Используйте run-gui.bat для Windows"; exit 1 ;;
esac
java --module-path "project/libs/javafx/$platform" --add-modules javafx.controls -jar PlayfairCipher.jar --gui
