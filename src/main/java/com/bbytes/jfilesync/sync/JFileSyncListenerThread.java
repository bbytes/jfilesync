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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * The thread that listens to all file related events like file creation , deletion etc . File copy
 * ,deletion etc is done in this class.
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

	private String destinationFolder;

	private String mode = "client";

	public JFileSyncListenerThread(JChannel fileSyncChannel) {
		this.fileSyncChannel = fileSyncChannel;
	}

	/**
	 * The wait param true is to make the main thread wait for this thread to close or return
	 * 
	 * @param fileSyncChannel
	 * @param wait
	 */
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
				log.debug("Received msg from " + msg.getSrc() + ": " + msg.getObject());
				if (!mode.equals("server")) {
					FileSyncMessage fileSyncMessage = (FileSyncMessage) msg.getObject();
					if (fileSyncMessage != null) {
						switch (fileSyncMessage.getFileMessageType()) {
						case FILE_CREATED:
							fileModified(fileSyncMessage);
							break;
						case FILE_DELETED:
							FileUtils.deleteQuietly(new File(destinationFolder + File.separator
									+ fileSyncMessage.getFileName()));
							break;
						case FILE_UPDATED:
							fileModified(fileSyncMessage);
							break;

						default:
							break;
						}
					}
				}

			}

			public void viewAccepted(View newView) {
				log.debug("Received new view " + newView);
			}

		});

		if (wait)
			latch.await();

		return true;

	}

	private void fileModified(FileSyncMessage fileSyncMessage){
		if (!fileSyncMessage.isDirectory()) {
			try {
				FileUtils.copyURLToFile(fileSyncMessage.getFileUrl().toURL(), new File(
						destinationFolder + File.separator + fileSyncMessage.getFileName()), 20000,
						20000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Close all service started by thread and release the wait lock on the caller thread
	 */
	public void shutDown() {
		fileSyncChannel.disconnect();
		latch.countDown();
	}

	/**
	 * The folder to which sync should happen on client side
	 * 
	 * @return the destinationFolder
	 */
	public String getDestinationFolder() {
		return destinationFolder;
	}

	/**
	 * The folder to which sync should happen on client side
	 * 
	 * @param destinationFolder
	 *            the destinationFolder to set
	 */
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * Set mode .Possible values 'server' or 'client'
	 * 
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

}
