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
package com.bbytes.jfilesync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgroups.JChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bbytes.jfilesync.sync.JFileSyncListenerThread;

/**
 * The file sync client which listens to file modification messages and modifies the destination folder
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncClient {

	private ApplicationContext context;

	private JChannel fileSyncChannel;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private JFileSyncListenerThread fileSyncListenerThread;
	
	private String destinationFolder;

	public JFileSyncClient() throws Exception {
		this.context = new ClassPathXmlApplicationContext("classpath:spring/jfilesync-client.xml");
		fileSyncChannel = context.getBean(JChannel.class);
		destinationFolder= (String) context.getBean("destinationFolder");
		fileSyncListenerThread = new JFileSyncListenerThread(fileSyncChannel);
		fileSyncListenerThread.setDestinationFolder(destinationFolder);
		fileSyncListenerThread.setMode("client");
	}

	public void start() throws Exception {
		addShutDownHook();
		executor.submit(fileSyncListenerThread);
	}

	/**
	 * Add shutdown hook to the client
	 */
	private void addShutDownHook() {
		// shutdown event
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				shutDown();
			}
		}));

	}

	/**
	 * Close all resource in client 
	 */
	public void shutDown() {
		fileSyncListenerThread.shutDown();
		executor.shutdown();
	}

}
