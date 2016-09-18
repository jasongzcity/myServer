@echo off

rem the shared script for "startup" and "shutdown" the server
rem author lwz

setlocal

rem we got SERVER_HOME now
rem now got the JAVA_HOME and set command line arguments
rem user should add their own command line arguments & java options at setClasspath.bat
if not exist "%SERVER_HOME%\bin\setClasspath.bat" goto noClasspath
call %SERVER_HOME%\bin\setClasspath.bat
if errorlevel 1 goto end

rem set up the needed external lib in class path
set "CLASSPATH=%USER_CLASSPATH%%CLASSPATH%%SERVER_HOME%\lib\el-api.jar;%SERVER_HOME%\lib\log4j-api.jar;%SERVER_HOME%\lib\log4j-core.jar;%SERVER_HOME%\lib\servlet-api.jar;%SERVER_HOME%\classes"

rem set up java options
set "JAVA_OPTS=%SERVER_OPTS% %USER_JAVAOPTS%"

:setArgs
if "%1"=="" goto argsSetted
set "CMD_ARGS=%CMD_ARGS% %1"
shift
goto setArgs

:argsSetted
set "CMD_ARGS=%CMD_ARGS% %USER_CMD_ARGS%"

REM==============JAVA COMMAND============

echo 
echo Using CLASSPATH: %CLASSPATH%
echo Using JAVA OPTIONS: %JAVA_OPTS%
echo Using Command line arguments: %CMD_ARGS%
echo Using Jvm: %_RUNJAVA%
echo 

set "MAINCLASS=com.jason.server.connector.Bootstrap"

start "MyServer" %_RUNJAVA% %JAVA_OPTS% -classpath "%CLASSPATH%" -Dserver.base=%SERVER_HOME% %MAINCLASS% %CMD_ARGS%
goto end

:noClassPath
echo can't find %SERVER_HOME%\bin\setClasspath.bat in given path
echo set the right path as BUILDING_GUIDE.TXT specified
goto end

:end