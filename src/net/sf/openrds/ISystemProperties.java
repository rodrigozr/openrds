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
 * 
 * ISystemProperties.java
 * Created by: Rodrigo
 * Created at: Sep 26, 2005 2:14:47 PM
 *
 * $Revision: 1.4 $
 * $Date: 2006/09/21 19:13:50 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

/**
 * Holds constants that indentify system properties read by OpenRDS.
 * @author Rodrigo
 */
public interface ISystemProperties {
	/**
	 * This property can be used on machines with more than one network adaptor
	 * to set which one to use.<BR><BR>
	 * Eg: Suppose you have one adaptor with ip "200.206.xxx.xxx" (external ip)
	 * and another one with ip "192.168.xxx.xxx" (LAN ip).<BR>
	 * You can set this property to <b><code>"192."</code></b> to tell the
	 * distributed system to use the network adaptor connected on the LAN.<BR><BR>
	 * The system will use the first network interface found that <B>STARTS</B> with
	 * the value of this property, so you don't need to specify the full ip address.<BR>
	 * If the system cannot find any matching network interface, it will use the
	 * loopback interface provided by the operating system (usually "127.0.0.1").
	 * @value "openrds.base.ip"
	 */
	String BASE_IP = "openrds.base.ip";
	/**
	 * This property can be used to set the memory amount of all nodes started on
	 * this JVM.<BR>
	 * If this property is not set when calling <b><code>NodeFactory.startProcessNode()</code></b>,
	 * it will use a JNI library to determine the physical memory of this machine.
	 * @value "openrds.memory.amount"
	 */
	String MEMORY_AMOUNT = "openrds.memory.amount";
	/**
	 * This property can be used to set the clock amount (MHz) of all nodes started on
	 * this JVM.<BR>
	 * If this property is not set when calling <b><code>NodeFactory.startProcessNode()</code></b>,
	 * it will use a JNI library to determine the clock in MHz.
	 * @value "openrds.clock.amount"
	 */
	String CLOCK_AMOUNT = "openrds.clock.amount";
	/**
	 * This property can be used to override the default connection verification interval
	 * (in milliseconds) between a process node and the main node.<BR>
	 * This verification is used to detect disconnections. Everytime a disconnection is
	 * detected, the node will keep trying to restabilish it continuously with a shorter
	 * interval. (See RECONNECTION_INTERVAL).<BR>
	 * The default interval is 30000 (30 seconds).
	 * @value "openrds.verification.interval"
	 */
	String VERIFICATION_INTERVAL = "openrds.verification.interval";
	/**
	 * This property can be used to override the default interval for reconnection
	 * tentatives.<BR>
	 * A process node enters in a "disconnected" mode after detecting any communication
	 * error with the main node, after that it will keep trying to restabilish the connection
	 * continuously at every interval.<BR>
	 * The default reconnection interval is 5000 (5 seconds).
	 * @value "openrds.reconnection.interval"
	 */
	String RECONNECTION_INTERVAL = "openrds.reconnection.interval";
	
	/**
	 * This property can be used to define the timeout for "create socket" operations.<BR>
	 * All remote method calls are handled by socket transport layer.<BR>
	 * If a node crashes, it will not be possible to connect to it anymore, but this situation
	 * is only detected when a socket timeout happens.<BR>
	 * The default "create socket" timeout is 10000 (10 seconds).<BR>
	 * You can lower/raise this value on more/less reliable environments.
	 * @value "openrds.connect.timeout"
	 * @since OpenRDS 1.1-beta
	 */
	String CONNECT_TIMEOUT = "openrds.connect.timeout";
}
