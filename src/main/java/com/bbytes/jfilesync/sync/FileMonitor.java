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
import java.net.URI;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * Monitors the source file for modification anf fires file messages
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FileMonitor extends FileAlterationListenerAdaptor {

	private JChannel fileSyncChannel;

	private String destFolderToMonitor;

	private long intervalInSeconds = 5;

	private FileAlterationMonitor monitor;
	
	private String serverUrl; 

	public void start() throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(destFolderToMonitor);
		observer.addListener(this);
		FileAlterationMonitor monitor = new FileAlterationMonitor(intervalInSeconds);
		monitor.addObserver(observer);
		monitor.start();
	}

	public void stop() throws Exception {
		monitor.stop();
	}

	public void onFileChange(File file) {
		fileModified(file,FileMessageType.FILE_UPDATED);
	}

	public void onFileCreate(File file) {
		fileModified(file,FileMessageType.FILE_CREATED);
	}

	public void onFileDelete(File file) {
		fileModified(file,FileMessageType.FILE_DELETED);
	}

	private void fileModified(File file,FileMessageType fileMessageType) {
		try {
			String filePath = file.toURI().toString();
			File desFolder = new File(destFolderToMonitor);
			filePath = filePath.replace(desFolder.toURI().toString(), "");
			URI fileDownloadURL = new URI(serverUrl + filePath);
			FileSyncMessage fileSyncMessage = new FileSyncMessage(fileMessageType, fileDownloadURL,
					file.getName());
			fileSyncChannel.send(new Message(null, null, fileSyncMessage));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the destFolderToMonitor
	 */
	public String getDestFolderToMonitor() {
		return destFolderToMonitor;
	}

	/**
	 * @param destFolderToMonitor
	 *            the destFolderToMonitor to set
	 */
	public void setDestFolderToMonitor(String destFolderToMonitor) {
		this.destFolderToMonitor = destFolderToMonitor;
	}

	/**
	 * @return the intervalInSeconds
	 */
	public long getIntervalInSeconds() {
		return intervalInSeconds;
	}

	/**
	 * @param intervalInSeconds
	 *            the intervalInSeconds to set
	 */
	public void setIntervalInSeconds(long intervalInSeconds) {
		this.intervalInSeconds = intervalInSeconds;
	}

	/**
	 * @return the fileSyncChannel
	 */
	public JChannel getFileSyncChannel() {
		return fileSyncChannel;
	}

	/**
	 * @param fileSyncChannel
	 *            the fileSyncChannel to set
	 */
	public void setFileSyncChannel(JChannel fileSyncChannel) {
		this.fileSyncChannel = fileSyncChannel;
	}

	/**
	 * @return the serverUrl
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @param serverUrl the serverUrl to set
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
