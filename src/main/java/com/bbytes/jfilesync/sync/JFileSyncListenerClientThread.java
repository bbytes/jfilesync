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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.file.FileSystemException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import com.bbytes.jfilesync.sync.ftp.FTPClientFactory;

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

	private FTPClientFactory ftpClientFactory;

	private FTPClient ftpClient;

	/**
	 * @param fileSyncChannel
	 */
	public JFileSyncListenerClientThread(JChannel fileSyncChannel) {
		super(fileSyncChannel);
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
					case DIRECTORY_STRUCTURE_SYNC:
						checkDirAndFiles(fileSyncMessage);
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
				if (ftpClient == null || !ftpClient.isConnected()) {
					ftpClient = ftpClientFactory.getClientInstance();
				}
				log.debug("Received node state " + newView);
			}
		});

		latch.await();

		return true;

	}

	private void checkDirAndFiles(FileSyncMessage fileSyncMessage) {
		// TODO : need to get the logic done ..checksum should be used
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

	private File getLocalFile(FileSyncMessage fileSyncMessage) throws FileSystemException {
		File theRelativeDir = null;
		File localFile = null;
		if (fileSyncMessage.getBaseFolderRelativePath() != null
				& fileSyncMessage.getBaseFolderRelativePath().length() > 0) {
			theRelativeDir = new File(new File(destinationFolder).getPath()
					+ fileSyncMessage.getBaseFolderRelativePath());
		}

		if (theRelativeDir != null) {
			localFile = new File(theRelativeDir.getPath() + File.separator + fileSyncMessage.getFileName());
		} else {
			localFile = new File(new File(destinationFolder).getPath() + File.separator + fileSyncMessage.getFileName());
		}

		return localFile;
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
					copyFileToDestinationFolder(theDir, fileSyncMessage);
				} else {
					copyFileToDestinationFolder(new File(destinationFolder), fileSyncMessage);
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

	public void copyFileToDestinationFolder(File dir, FileSyncMessage fileSyncMessage) {
		try {
			if (ftpClient == null || !ftpClient.isConnected()) {
				ftpClient = ftpClientFactory.getClientInstance();
				if (!ftpClient.isConnected()) {
					throw new ConnectException("FTP client not connected. Make sure FTP server is running");
				}
			}
			FTPFile file = ftpClient.mlistFile(fileSyncMessage.getBaseFolderRelativePath() + "/"
					+ fileSyncMessage.getFileName());
			OutputStream output;
			output = new FileOutputStream(dir.getPath() + File.separator + file.getName());
			// get the file from the remote system
			ftpClient.retrieveFile(file.getName(), output);
			// close output stream
			output.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the ftpClientFactory
	 */
	public FTPClientFactory getFtpClientFactory() {
		return ftpClientFactory;
	}

	/**
	 * @param ftpClientFactory
	 *            the ftpClientFactory to set
	 */
	public void setFtpClientFactory(FTPClientFactory ftpClientFactory) {
		this.ftpClientFactory = ftpClientFactory;
	}

}
