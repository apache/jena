@echo off
@rem Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

if "%JENAROOT%" == "" goto :rootNotSet
set JENA_HOME=%JENAROOT%
:rootNotSet

if NOT "%JENA_HOME%" == "" goto :okHome
echo JENA_HOME not set
exit /B

:okHome
set JENA_CP=%JENA_HOME%\lib\*;
set LOGGING=file:%JENA_HOME%/log4j2.properties

if "%CLASSPATH%" == "" goto :noExtra
set JENA_CP="%JENA_CP%:%CLASSPATH%"
:noExtra

@rem JVM_ARGS comes from the environment.
java %JVM_ARGS% -Dlog4j.configurationFile="%LOGGING%" -cp "%JENA_CP%" arq.rupdate %*
exit /B
