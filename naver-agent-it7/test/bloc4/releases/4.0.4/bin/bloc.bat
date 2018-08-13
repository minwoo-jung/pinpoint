@echo off
rem -----------------------------------------------------------------------------
rem Start/Stop Script for the BLOC Server
rem -----------------------------------------------------------------------------


set "CURRENT_DIR=%cd%"
if not "%BLOC_HOME%" == "" goto gotHome
set "BLOC_HOME=%CURRENT_DIR%"

cd ..
set "BLOC_HOME=%cd%"

:gotHome

if not "%BLOC_HOME%" == "" set "BLOC_BASE=%BLOC_HOME%"

"%JAVA_HOME%"\bin\java -Dbloc.home="%BLOC_HOME%" -Dbloc.base="%BLOC_BASE%" -Djava.io.tmpdir="%BLOC_BASE%"/temp -jar "%BLOC_HOME%"\libs\bloc-server-4.0.4.jar "%BLOC_BASE%"/conf/bloc.ini "%*"