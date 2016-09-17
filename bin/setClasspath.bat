@echo off

rem find executable jvm entrance and set the command line arguments
rem author lwz

rem find jvm entrance
if "%JAVA_HOME%"=="" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJava

set _RUNJAVA="%JAVA_HOME%\bin\java.exe"

rem user should add external classpath here if needed,remember to add semicolon at the end
set USER_CLASSPATH=

rem user should add java options here
set USER_JAVAOPTS=

rem user should add command line arguments here 
set USER_CMD_ARGS=

goto end

:noJavaHome
echo can't find JAVA_HOME as an environmental variable
goto exit

:noJava
echo can't find executable java.exe
goto exit

:exit
exit /b 1

:end
exit /b 0