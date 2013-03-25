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

import org.jgroups.Channel;
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
	private String port;
	private String bindAddress;
	private String gossipPort;
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
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the bindAddress
	 */
	public String getBindAddress() {
		return bindAddress;
	}

	/**
	 * @param bindAddress the bindAddress to set
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	/**
	 * @return the gossipPort
	 */
	public String getGossipPort() {
		return gossipPort;
	}

	/**
	 * @param gossipPort the gossipPort to set
	 */
	public void setGossipPort(String gossipPort) {
		this.gossipPort = gossipPort;
	}

	/**
	 * @return the gossipBindAddress
	 */
	public String getGossipBindAddress() {
		return gossipBindAddress;
	}

	/**
	 * @param gossipBindAddress the gossipBindAddress to set
	 */
	public void setGossipBindAddress(String gossipBindAddress) {
		this.gossipBindAddress = gossipBindAddress;
	}

	public Class<Channel> getObjectType() {
		return Channel.class;
	}

	protected JChannel createInstance() throws Exception {

		// Channel jChannel = factory.createChannel();
		// jChannel.connect(clusterName);
		//
		// return jChannel;
		TCPGOSSIP gossip = new TCPGOSSIP();
		List<InetSocketAddress> initial_hosts = new ArrayList<InetSocketAddress>();
		initial_hosts.add(new InetSocketAddress(5559));
		gossip.setInitialHosts(initial_hosts);

		JChannel channel = new JChannel(new TCP().setValue("use_send_queues", true).setValue("sock_conn_timeout", 300),
				gossip, new MERGE2().setValue("min_interval", 1000).setValue("max_interval", 3000), new FD().setValue(
						"timeout", 2000).setValue("max_tries", 2), new VERIFY_SUSPECT(), new NAKACK2().setValue(
						"use_mcast_xmit", false), new UNICAST2(), new STABLE(), new GMS());
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
