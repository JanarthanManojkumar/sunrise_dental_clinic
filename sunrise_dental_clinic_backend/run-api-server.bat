@echo off
setlocal
cd /d "%~dp0"
java -cp "build\classes;lib\*" api.ApiServer
