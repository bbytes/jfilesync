/*
 * Copyright (C) 2013 The Zorba Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.bbytes.jfilesync.jgroup;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.protocols.FD;
import org.jgroups.protocols.MERGE2;
import org.jgroups.protocols.TCP;
import org.jgroups.protocols.TCPGOSSIP;
import org.jgroups.protocols.UNICAST2;
import org.jgroups.protocols.VERIFY_SUSPECT;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version 1.0
 */
public class ChannelBeanFactory extends AbstractFactoryBean<JChannel> implements DisposableBean {

	private String clusterName;
	private int port;
	private int gossipPort;
	private String gossipBindAddress;

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName
	 *            the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}


	/**
	 * @return the gossipPort
	 */
	public int getGossipPort() {
		return gossipPort;
	}

	/**
	 * @param gossipPort
	 *            the gossipPort to set
	 */
	public void setGossipPort(int gossipPort) {
		this.gossipPort = gossipPort;
	}

	/**
	 * @return the gossipBindAddress
	 */
	public String getGossipBindAddress() {
		return gossipBindAddress;
	}

	/**
	 * @param gossipBindAddress
	 *            the gossipBindAddress to set
	 */
	public void setGossipBindAddress(String gossipBindAddress) {
		this.gossipBindAddress = gossipBindAddress;
	}

	public Class<JChannel> getObjectType() {
		return JChannel.class;
	}

	protected JChannel createInstance() throws Exception {

		// Channel jChannel = factory.createChannel();
		// jChannel.connect(clusterName);
		//
		// return jChannel;
		TCPGOSSIP gossip = new TCPGOSSIP();
		List<InetSocketAddress> initial_hosts = new ArrayList<InetSocketAddress>();
		if (gossipBindAddress == null || gossipBindAddress.trim().length() == 0) {
			initial_hosts.add(new InetSocketAddress(gossipPort));
		} else {
			initial_hosts.add(new InetSocketAddress(gossipBindAddress, gossipPort));
		}

		gossip.setInitialHosts(initial_hosts);

		JChannel channel = new JChannel(new TCP().setValue("use_send_queues", true).setValue("sock_conn_timeout", 300)
				.setValue("bind_port", port), gossip, new MERGE2().setValue("min_interval", 1000).setValue(
				"max_interval", 3000), new FD().setValue("timeout", 2000).setValue("max_tries", 2),
				new VERIFY_SUSPECT(), new NAKACK2().setValue("use_mcast_xmit", false), new UNICAST2(), new STABLE(),
				new GMS());
		channel.connect(clusterName);
		return channel;
	}

	protected void destroyInstance(JChannel instance) throws Exception {
		super.destroyInstance(instance);
		instance.close();
	}

	public void destroy() throws Exception {
		super.destroy();
	}
}
