@echo off
@REM Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0
@REM Script to run a command

if NOT "%ARQROOT%" == "" goto :okRoot
echo ARQROOT not set
exit /B

:okRoot
call %ARQROOT%\bat\make_classpath.bat %ARQROOT%

java -cp %CP% arq.query %*
exit /B
