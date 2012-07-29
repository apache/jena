@echo off
@rem Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

if NOT "%JENAROOT%" == "" goto :okRoot
echo JENAROOT not set
exit /B

:okRoot

set JVM_ARGS=-Xmx1024M
set JENA_CP=%JENAROOT%\lib\*;
set LOGGING=file:%JENAROOT%/jena-log4j.properties

java %JVM_ARGS% -Dlog4j.configuration="%LOGGING%" -cp "%JENA_CP%" tdb.tdbupdate %*
exit /B
