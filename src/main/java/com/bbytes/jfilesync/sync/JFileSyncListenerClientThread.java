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
import java.nio.file.FileSystemException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * * The thread that listens to all file related events like file creation , deletion etc . File
 * copy ,deletion etc is done in this class.
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncListenerClientThread extends JFileSyncListenerServerThread {

	private static final Logger log = Logger.getLogger(JFileSyncListenerClientThread.class);

	private final CountDownLatch latch = new CountDownLatch(1);

	private String destinationFolder;

	/**
	 * @param fileSyncChannel
	 */
	public JFileSyncListenerClientThread(JChannel fileSyncChannel) {
		super(fileSyncChannel);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public Boolean call() throws Exception {

		fileSyncChannel.setReceiver(new ReceiverAdapter() {
			public synchronized void receive(Message msg) {
				log.debug("Received msg from " + msg.getSrc() + ": " + msg.getObject());

				FileSyncMessage fileSyncMessage = (FileSyncMessage) msg.getObject();
				if (fileSyncMessage != null) {
					switch (fileSyncMessage.getFileMessageType()) {
					case FILE_CREATED:
						fileModified(fileSyncMessage);
						break;
					case FILE_DELETED:
						fileDeleted(fileSyncMessage);
						break;
					case FILE_UPDATED:
						fileModified(fileSyncMessage);
						break;

					default:
						break;
					}
				}

			}

			/**
			 * view is the new server or client node that has joined or even when a server or client
			 * node is deleted this is called
			 */
			public void viewAccepted(View newView) {
				log.debug("Received node state " + newView);
			}

		});

		latch.await();

		return true;

	}

	private void fileDeleted(FileSyncMessage fileSyncMessage) {
		if (fileSyncMessage.isDirectory()) {
			FileUtils.deleteQuietly(new File(destinationFolder + File.separator
					+ fileSyncMessage.getBaseFolderRelativePath()));
		} else {
			FileUtils.deleteQuietly(new File(destinationFolder + File.separator
					+ fileSyncMessage.getBaseFolderRelativePath() + fileSyncMessage.getFileName()));
		}

	}

	private void fileModified(FileSyncMessage fileSyncMessage) {
		if (!fileSyncMessage.isDirectory()) {
			File theDir = null;
			try {
				if (fileSyncMessage.getBaseFolderRelativePath() != null
						& fileSyncMessage.getBaseFolderRelativePath().length() > 0) {
					theDir = new File(new File(destinationFolder).getPath()
							+ fileSyncMessage.getBaseFolderRelativePath());
					// if the directory does not exist, create it
					if (!theDir.exists()) {
						boolean result = theDir.mkdirs();
						if (!result) {
							throw new FileSystemException("Directory creation failed : "
									+ new File(destinationFolder).getPath()
									+ fileSyncMessage.getBaseFolderRelativePath());
						}
					}
				}
				if (theDir != null) {
					FileUtils.copyURLToFile(fileSyncMessage.getFileUrl().toURL(), new File(theDir.getPath()
							+ File.separator + fileSyncMessage.getFileName()), 20000, 20000);
				} else {
					FileUtils.copyURLToFile(
							fileSyncMessage.getFileUrl().toURL(),
							new File(new File(destinationFolder).getPath() + File.separator
									+ fileSyncMessage.getFileName()), 20000, 20000);
				}

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
}
