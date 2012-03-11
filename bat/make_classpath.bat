@echo off
@REM Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0
@REM Script to build a classpath

set DIRROOT=%1%
set CP=

if "%DIRROOT%" == ""  goto noRoot

set LIBDIR=%DIRROOT%\lib
set CLSDIR=%DIRROOT%\classes

for %%f in (%LIBDIR%\*.jar) do call :oneStep %%f

if EXIST %CLSDIR% call :addClasses
goto :theEnd

:oneStep
REM echo "ARG: %1"
if "%CP%" == "" (set CP=%1) else (set CP=%CP%;%1)
exit /B

:addClasses
if "%CP%" == "" (set CP=%CLSDIR%) else (set CP=%CLSDIR%;%CP%)
exit /B

:noRoot
echo No directory for root of installation
exit /B

:theEnd
REM echo %CP%
exit /B
