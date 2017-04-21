@echo off
rem /**
rem  * OpenRDS - Open Requisition Distribution System
rem  * Copyright (c) 2006 Rodrigo Zechin Rosauro
rem  * 
rem  * This software program is free software; you can redistribute it and/or modify it under the
rem  * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
rem  * either version 2.1 of the License, or (at your option) any later version.
rem  * 
rem  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
rem  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
rem  * See the GNU Lesser General Public License for more details.
rem  * 
rem  * clusterNode.bat
rem  * Created by: Rodrigo Rosauro
rem  * Created at: 19/09/2006 20:51:43
rem  *
rem  * $Revision: 1.5 $
rem  * $Date: 2006/10/03 02:02:18 $ (of revision)
rem  * $Author: rodrigorosauro $ (of revision)
rem  */

rem ##############################################
rem #### CHANGE THESE PROPERTIES IF NECESSARY ####
rem ##############################################

rem # Java executable location
rem # E.g: JAVA_BIN=C:\Program Files\Java\j2re1.4.2_05\bin\
rem # The default value is %JAVA_HOME%\bin\ (if JAVA_HOME is set)
rem # Don't forget to add the last '\' character
SET JAVA_BIN=

rem # Add java arguments here. E.g.: JAVA_ARGS=-Dopenrds.base.ip=192.
SET JAVA_ARGS=

rem # Add additional JAR files here separating by ';'. E.g.: LIBS=my-app.jar;other-lib.jar
SET LIBS=

rem # Change this to modify the max java heap size
SET MAX_JAVA_HEAP=256M

rem #########################################
rem #### DO NOT MODIFY BEYOND THIS POINT ####
rem #########################################

if "%JAVA_BIN%" == "" if not "%JAVA_HOME%" == "" SET JAVA_BIN=%JAVA_HOME%\bin\
SET JAVA_EXE="%JAVA_BIN%java.exe"
if not "%LIBS%" == "" SET LIBS=;%LIBS%

:BEGIN
IF EXIST "openrds.updated.jar" GOTO UPDATE
GOTO START

:START
%JAVA_EXE% -Xmx%MAX_JAVA_HEAP% %JAVA_ARGS% -cp OpenRDS.jar%LIBS% net.sf.openrds.tools.ClusterNode
if ERRORLEVEL 172 GOTO EXIT
if ERRORLEVEL 171 GOTO BEGIN
GOTO EXIT

:UPDATE
echo Making backup file OpenRDS.jar.bkp
move /Y OpenRDS.jar OpenRDS.jar.bkp
if ERRORLEVEL 1 GOTO ERROR_BKP
echo Updating OpenRDS.jar
move /Y openrds.updated.jar OpenRDS.jar
if ERRORLEVEL 1 GOTO ERROR_UPDT
GOTO START

:ERROR_BKP
echo [FATAL] - Could not copy file OpenRDS.jar to OpenRDS.jar.bkp
echo           Please check if the file is not being used.
GOTO EXIT

:ERROR_UPDT
echo [FATAL] - Could not rename file openrds.updated.jar to OpenRDS.jar
echo           Please check if the file is not being used.
GOTO EXIT

:EXIT
