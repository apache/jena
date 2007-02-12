@echo off
@REM Script to run a command

if NOT "%ARQROOT%" == "" goto :okRoot
echo ARQROOT not set
exit /B

:okRoot
call %ARQROOT%\bat\make_classpath.bat %ARQROOT%

java -cp %CP% arq.qtest %1 %2 %3 %4 %5 %6 %7 %8 %9
exit /B
