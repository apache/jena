@echo off
@rem Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

if "%JENAROOT%" == "" goto :rootNotSet
set JENA_HOME=%JENAROOT%
:rootNotSet

if NOT "%JENA_HOME%" == "" goto :okHome
echo JENA_HOME not set
exit /B

:okHome
set JVM_ARGS=-Xmx1024M
set JENA_CP=%JENA_HOME%\lib\*;
set LOGGING=file:%JENA_HOME%/jena-log4j.properties

java %JVM_ARGS% -Dlog4j.configuration="%LOGGING%" -cp "%JENA_CP%" tdb.tdbbackup %*
exit /B
