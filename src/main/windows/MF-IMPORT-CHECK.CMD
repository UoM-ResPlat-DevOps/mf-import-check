@echo off

if not exist %~dp0\mf-import-check.jar echo "Error: could not find mf-import-check.jar"

java -cp %~dp0\mf-import-check.jar vicnode.checker.cli.MFImportCheck %*
