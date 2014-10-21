@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.

@REM Usage: make_classpath DIR
@REM Finds jars in lib/, and class files in classes/ and build/classes
@REM If CP is already set, include that as well.

@echo off
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

echo "Set the classpath in make_classpath.bat"

exit /B
