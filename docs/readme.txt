/**
 * OpenRDS - Open Requisition Distribution System
 * Copyright (c) 2006 Rodrigo Zechin Rosauro
 * 
 * This software program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 */
 
Package files' description:
- OpenRDS.jar:       JAR compiled without debug information.
- OpenRDS_debug.jar: JAR compiled with debug information.
- OpenRDS-src.zip:   Source-code for this version.
- OpenRDS.dll:       Windows JNI for getting processor speed and memory amount.
- clusterNode.bat:   Windows batch file for executing the "cluster node tool"
- clusterNode.sh:    Linux shell script for executing the "cluster node tool"
- changelog.txt:     Change-log for this version.
- readme.txt:        This file :)

USEFUL COMMANDS TO RUN:
-----------------------
(Starts a generic main node)
java -cp OpenRDS.jar net.sf.openrds.examples.GenericMainNode

(Starts a generic process node and connects it to a main node on the local machine)
java -cp OpenRDS.jar net.sf.openrds.examples.GenericProcessNode 127.0.0.1

(Runs a hello-world application to test connection with a main node on the local machine)
java -cp OpenRDS.jar net.sf.openrds.examples.HelloWorld 127.0.0.1

(Closes ALL started process nodes - By closing their JVM)
java -cp OpenRDS.jar net.sf.openrds.examples.CloseAll 127.0.0.1

See OpenRDS documentation at http://openrds.sf.net for more details.