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
package com.bbytes.jfilesync.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncListenerThread implements Callable<Boolean> {

	private static final Logger log = Logger.getLogger(JFileSyncListenerThread.class);
	
	private final CountDownLatch latch = new CountDownLatch(1);

	private JChannel fileSyncChannel;

	private boolean wait = true;

	public JFileSyncListenerThread(JChannel fileSyncChannel) {
		this.fileSyncChannel = fileSyncChannel;
	}

	public JFileSyncListenerThread(JChannel fileSyncChannel, boolean wait) {
		this.wait = wait;
		this.fileSyncChannel = fileSyncChannel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public Boolean call() throws Exception {

		fileSyncChannel.setReceiver(new ReceiverAdapter() {
			public void receive(Message msg) {
				log.debug("received msg from " + msg.getSrc() + ": " + msg.getObject());
			}

			public void viewAccepted(View newView) {
				log.debug("received new view " + newView.toString());
			}

		});

		if (wait)
			latch.await();

		return true;

	}

	public void shutDown() {
		fileSyncChannel.disconnect();
		latch.countDown();
	}

}
