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

import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * The thread that listens to the new node joining the client server sync node group
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncListenerServerThread implements Callable<Boolean> {

	private static final Logger log = Logger.getLogger(JFileSyncListenerServerThread.class);

	protected JChannel fileSyncChannel;

	public JFileSyncListenerServerThread(JChannel fileSyncChannel) {
		this.fileSyncChannel = fileSyncChannel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public Boolean call() throws Exception {

		fileSyncChannel.setReceiver(new ReceiverAdapter() {

			/**
			 * view is the new client node that has joined or even when a client node is deleted
			 * this is called
			 */
			public void viewAccepted(View newView) {
				log.debug("Received node state " + newView);
			}

		});

		return true;

	}

	/**
	 * Close all service started by thread and release the wait lock on the caller thread
	 */
	public void shutDown() {
		fileSyncChannel.disconnect();
	}

}
