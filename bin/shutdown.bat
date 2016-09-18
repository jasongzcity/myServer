@echo off

rem "shutdown" scricpt for MyServer
rem author lwz

setlocal

rem Guess the SERVER_HOME setted.
rem If not, use the current directory or the parent directory of current directory
if not "%SERVER_HOME%" == "" goto gotHome

rem set method use reference
rem guess home at current dir
set "CUR_DIR=%cd%"
set "SERVER_HOME=%CUR_DIR%"
if exist "%SERVER_HOME%\bin\startInternal.bat" goto gotInternal
cd ..
set "SERVER_HOME=%cd%"
if exist "%SERVER_HOME%\bin\startInternal.bat" goto gotInternal
goto noInternal

:gotHome
if exist "%SERVER_HOME%\bin\startInternal.bat" goto gotInternal
goto noInternal

:noInternal
echo can't find %SERVER_HOME%\bin\startInternal.bat
echo set the right path as BUILDING_GUIDE.TXT specified
goto end

:gotInternal
set "EXECUTABLE=%SERVER_HOME%\bin\startInternal.bat" 
goto okInternal

rem "shutdown" the server
:okInternal
call "%EXECUTABLE%" shutdown

:end