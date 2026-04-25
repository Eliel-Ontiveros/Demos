@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup batch script, version 3.3.2

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0")

@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@SET WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

@FOR /F "tokens=2 delims==" %%G IN ('findstr /i "distributionUrl" "%WRAPPER_PROPERTIES%"') DO (
  SET "DISTRIBUTION_URL=%%G"
)

@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
@SET MAVEN_WRAPPER_HOME=%MAVEN_USER_HOME%\wrapper\dists

@IF NOT DEFINED JAVA_HOME (
  ECHO Error: JAVA_HOME is not set. 1>&2
  EXIT /B 1
)

@SET JAVA_CMD=%JAVA_HOME%\bin\java.exe

@ECHO Downloading Maven...
@POWERSHELL -Command "& { Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%TEMP%\maven.zip' }"
@POWERSHELL -Command "& { Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%MAVEN_WRAPPER_HOME%' -Force }"

@FOR /R "%MAVEN_WRAPPER_HOME%" %%F IN (mvn.cmd) DO (
  SET "MVN_CMD=%%F"
  GOTO :found
)
:found

@CALL "%MVN_CMD%" %*
