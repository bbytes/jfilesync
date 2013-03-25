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

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncClient extends ReceiverAdapter {

	private ApplicationContext context;

	private Channel fileSyncChannel;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	JFileSyncClientThread clientThread = new JFileSyncClientThread();

	public JFileSyncClient() throws ChannelException {
		this.context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
		fileSyncChannel = context.getBean(Channel.class);
	}

	public void start() throws Exception {
		addShutDownHook();
		executor.submit(clientThread);
	}

	/**
	 * 
	 */
	private void addShutDownHook() {
		// shutdown event
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				shutDown();
			}
		}));

	}

	public void shutDown() {
		clientThread.shutDown();
		executor.shutdown();
	}

	public class JFileSyncClientThread implements Callable<Boolean> {

		private final CountDownLatch latch = new CountDownLatch(1);

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public Boolean call() throws Exception {

			fileSyncChannel.setReceiver(new ReceiverAdapter() {
				public void receive(Message msg) {
					System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
				}

				public void viewAccepted(View newView) {
					System.out.println("received new view " + newView.printDetails());
				}

			});

			latch.await();

			return true;

		}

		public void shutDown() {
			fileSyncChannel.disconnect();
			latch.countDown();
		}

	}
}
