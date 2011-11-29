@echo off
@REM Script to run a command

if NOT "%JENAROOT%" == "" goto :okRoot
echo JENAROOT not set
exit /B

:okRoot
call %JENAROOT%\bat\make_classpath.bat %JENAROOT%

java %SOCKS% -cp %CP% arq.qparse %*
exit /B
