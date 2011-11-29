@echo off

if NOT "%JENAROOT%" == "" goto :okRoot
echo JENAROOT not set
exit /B

:okRoot

set JVM_ARGS=--Xmx1024M
set JENA_CP=%JENAROOT\lib\*
set LOGGING=-Dlog4j.configuration=file:%JENAROOT%/log4j.properties

java %JVM_ARGS% %LOGGING% -cp %$JENA_CP% arq.wwwenc %*
exit /B
