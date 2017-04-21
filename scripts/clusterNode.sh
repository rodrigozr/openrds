#!/bin/sh
#
# OpenRDS - Open Requisition Distribution System
# Copyright (c) 2006 Rodrigo Zechin Rosauro
# 
# This software program is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free Software Foundation;
# either version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU Lesser General Public License for more details.
# 
# clusterNode.sh
# Created by: Rodrigo
# Created at: Sep 20, 2006 21:37:06
#
# $Revision: 1.4 $
# $Date: 2006/10/03 02:02:53 $ (of revision)
# $Author: rodrigorosauro $ (of revision)
##

##############################################
#### CHANGE THESE PROPERTIES IF NECESSARY ####
##############################################

# Java executable location
# E.g: JAVA_BIN=/usr/java/j2sdk1.4.2_03/
# The default value is $JAVA_HOME/bin/ (if JAVA_HOME is set)
# Don't forget to add the last '/' character
JAVA_BIN=

# Add java arguments here. E.g.: JAVA_ARGS=-Dopenrds.base.ip=192.
JAVA_ARGS=

# Add additional JAR files here separating by ':'. E.g.: LIBS=my-app.jar:other-lib.jar
LIBS=

# Change this to modify the max java heap size
MAX_JAVA_HEAP=256M

#########################################
#### DO NOT MODIFY BEYOND THIS POINT ####
#########################################
if [ "$JAVA_BIN" = "" ] && [ "$JAVA_HOME" != "" ]; then
	JAVA_BIN=$JAVA_HOME/bin/
fi
JAVA_EXE="$JAVA_BIN"java
EXIT_CODE_RESTART=171
if [ "$LIBS" = "" ]; then
	LIBS=:$LIBS
fi

#
# Main function
#
function main() {
	update
	runNode
}

#
# Runs the cluster node, setting the 'stop' variable to "true" if necessary.
#
function runNode() {
	$JAVA_EXE -Xmx$MAX_JAVA_HEAP $JAVA_ARGS -cp OpenRDS.jar$LIBS net.sf.openrds.tools.ClusterNode
	if [ $? != $EXIT_CODE_RESTART ]; then
		stop="true"
	fi
}

#
# Updates the JAR file
#
function update() {
	if [ -e "openrds.updated.jar" ]; then
		# The file exists, we must update
		echo Making backup file OpenRDS.jar.bkp
		mv OpenRDS.jar OpenRDS.jar.bkp
		checkError "Could not copy file OpenRDS.jar to OpenRDS.jar.bkp";
		echo Updating OpenRDS.jar
		mv openrds.updated.jar OpenRDS.jar
		checkError "Could not rename file openrds.updated.jar to OpenRDS.jar";
	fi
}

#
# Checks for an error and emits a FATAL message if necessary.
# Parameters:
#            [1]: Message to display
#
function checkError() {
	if [ "$?" != "0" ]; then
		# Last command failed
		echo [FATAL] - $1
		echo Please check if the file is not being used.
		exit 1
	fi
}

stop="false"
while [ $stop != "true" ]; do
	# Calls the main function while stop is not "true"
	main
done

exit 0