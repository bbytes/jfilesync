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

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.JChannelFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version 1.0 
 */
public class ChannelBeanFactory extends AbstractFactoryBean<Channel> implements DisposableBean {
	private String jgroupsConfig;
	private String clusterName;
	private JChannelFactory factory;

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * @return the jgroupsConfig
	 */
	public String getJgroupsConfig() {
		return jgroupsConfig;
	}

	/**
	 * @param jgroupsConfig the jgroupsConfig to set
	 */
	public void setJgroupsConfig(String jgroupsConfig) {
		this.jgroupsConfig = jgroupsConfig;
	}

	/**
	 * @return the factory
	 */
	public JChannelFactory getFactory() {
		return factory;
	}

	/**
	 * @param factory the factory to set
	 */
	public void setFactory(JChannelFactory factory) {
		this.factory = factory;
	}

	public Class<Channel> getObjectType() {
		return Channel.class;
	}

	protected Channel createInstance() throws Exception {
		if (factory == null)

			this.factory = new JChannelFactory(jgroupsConfig);

		Channel jChannel = factory.createChannel();
		jChannel.connect(clusterName);
		return jChannel;
	}

	protected void destroyInstance(Channel instance) throws Exception {
		super.destroyInstance(instance);
		if (instance instanceof JChannel) {
			JChannel jChannel = (JChannel) instance;
			jChannel.close();
		}
	}

	public void destroy() throws Exception {
		super.destroy();
	}
}
