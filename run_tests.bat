@echo off
setlocal

if not exist "bin" mkdir "bin"

dir /s /b src\*.java > sources.txt
javac -d bin -encoding UTF-8 @sources.txt
del sources.txt

java -cp bin com.tourist.test.SuggestionEngineTest
pause
