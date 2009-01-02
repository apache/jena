@echo off
@REM Script to run a command

if NOT "%ARQROOT%" == "" goto :okRoot
echo ARQROOT not set
exit /B

:okRoot
call %ARQROOT%\bat\make_classpath.bat %ARQROOT%

java -cp %CP% arq.rset %*
exit /B
