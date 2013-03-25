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
import java.net.URL;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FileMonitor implements FileAlterationListener {

	private JChannel fileSyncChannel;

	private String destFolderToMonitor;

	private long intervalInSeconds = 5;

	private FileAlterationMonitor monitor;

	
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

	public void onDirectoryChange(File file) {
		System.out.println("onDirectoryChange : " + file);
	}

	public void onDirectoryCreate(File file) {
		System.out.println("onDirectoryCreate : " + file);
		try {
			String filePath = file.getPath();
			filePath.replace(destFolderToMonitor, "");
			// URL url = new URL(file.getPath());
			FileSyncMessage fileSyncMessage = new FileSyncMessage(FileMessageType.FILE_CREATED, null, file.getName());
			fileSyncMessage.setDirectory(true);
			fileSyncChannel.send(new Message(null, null, fileSyncMessage));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onDirectoryDelete(File file) {
		System.out.println("onDirectoryDelete : " + file);
	}

	public void onFileChange(File file) {
		System.out.println("onFileChange : " + file);

	}

	public void onFileCreate(File file) {
		System.out.println("onFileCreate : " + file);
		try {

			String filePath = file.toURI().toString();
			File desFolder = new File(destFolderToMonitor);
			String destFilePath = desFolder.toURI().toString();
			System.out.println(destFilePath);
			System.out.println(filePath);
			filePath = filePath.replace(desFolder.toURI().toString(), "");

			URL fileDownloadURL = new URL("http://localhost:8090/" + filePath);
			FileSyncMessage fileSyncMessage = new FileSyncMessage(FileMessageType.FILE_CREATED, fileDownloadURL,
					file.getName());
			fileSyncChannel.send(new Message(null, null, fileSyncMessage));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onFileDelete(File file) {
		System.out.println("onFileDelete : " + file);

	}

	public void onStart(FileAlterationObserver file) {
		// System.out.println("onStart !!!!!");

	}

	public void onStop(FileAlterationObserver file) {
		// System.out.println("onStop !!!!!");
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
}
