@echo off
@REM Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0
@REM Script to run a command

if NOT "%JENAROOT%" == "" goto :okRoot
echo JENAROOT not set
exit /B

:okRoot
call %JENAROOT%\bat\make_classpath.bat %JENAROOT%

java %SOCKS% -cp %CP% jena.version %*
exit /B
