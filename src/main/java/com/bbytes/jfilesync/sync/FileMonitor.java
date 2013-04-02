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
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * Monitors the source file for modification and fire file messages
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FileMonitor extends FileAlterationListenerAdaptor {

	private static final Logger log = Logger.getLogger(FileMonitor.class);

	private JChannel fileSyncChannel;

	private String sourceFolderToMonitor;

	// interval to scan the source folder for changes 
	private long intervalInSeconds = 5;

	// interval to scan the dir structure and find checksum and push it to client
	private long publishDirectoryStrunctureInSeconds = 15;

	private FileAlterationMonitor monitor;

	private String serverUrl;

	private boolean running = true;

	public void start() throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(sourceFolderToMonitor);
		observer.addListener(this);
		monitor = new FileAlterationMonitor(intervalInSeconds);
		monitor.addObserver(observer);
		monitor.start();

		StructureSyncThread structureSyncThread = new StructureSyncThread();
		structureSyncThread.start();

	}

	public void stop() throws Exception {
		running = false;
		monitor.stop();
	}

	/**
	 * @return the sourceFolderToMonitor
	 */
	public String getSourceFolderToMonitor() {
		return sourceFolderToMonitor;
	}

	/**
	 * @param sourceFolderToMonitor
	 *            the sourceFolderToMonitor to set
	 */
	public void setSourceFolderToMonitor(String sourceFolderToMonitor) {
		this.sourceFolderToMonitor = sourceFolderToMonitor;
	}

	public void onFileChange(File file) {
		fileModified(file, FileMessageType.FILE_UPDATED, isDirectory(file));
	}

	public void onFileCreate(File file) {
		fileModified(file, FileMessageType.FILE_CREATED, isDirectory(file));
	}

	public void onFileDelete(File file) {
		fileModified(file, FileMessageType.FILE_DELETED, isDirectory(file));
	}

	public void onDirectoryDelete(File directory) {
		fileModified(directory, FileMessageType.FILE_DELETED, isDirectory(directory));
	}

	private synchronized void fileModified(File file, FileMessageType fileMessageType, boolean isDirectory) {
		try {

			File srcFolder = new File(sourceFolderToMonitor);

			String filePath = file.getPath();
			filePath = filePath.replace(srcFolder.getPath().toString(), "");

			int index = filePath.lastIndexOf(File.separator);
			String filename = null;
			String relativePath = null;
			if (!isDirectory) {
				filename = filePath.substring(index + 1);
				relativePath = filePath.replace(filename, "");
			} else {
				relativePath = filePath;
			}

			FileSyncMessage fileSyncMessage = new FileSyncMessage(fileMessageType, file.getName());
			fileSyncMessage.setBaseFolderRelativePath(relativePath);
			fileSyncMessage.setDirectory(isDirectory);
			fileSyncMessage.setOriginalFilePath(file.getPath());

			if (!file.isDirectory() && !fileMessageType.equals(FileMessageType.FILE_DELETED) && file.exists()
					&& file.canRead())
				fileSyncMessage.setChecksum(FileUtils.checksumCRC32(file));

			fileSyncChannel.send(new Message(null, null, fileSyncMessage));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private boolean isDirectory(File file) {
		if (!file.exists()) {
			// see if the file portion it doesn't have an extension
			return file.getName().lastIndexOf('.') == -1;
		} else {
			// see if the path that's already in place is a file or directory
			return file.isDirectory();
		}
	}

	/**
	 * Scan the entire source folder and send the lsit of files with checksum . if checksum is
	 * different on client side then these files are replaced by client
	 */
	public void syncFullDirsAndFiles() {
		Collection<File> allFiles = FileUtils.listFilesAndDirs(new File(sourceFolderToMonitor),
				TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
		for (File file : allFiles) {
			fileModified(file, FileMessageType.DIRECTORY_STRUCTURE_SYNC, file.isDirectory());
		}
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
	 * @param serverUrl
	 *            the serverUrl to set
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * @return the publishDirectoryStrunctureInSeconds
	 */
	public long getPublishDirectoryStrunctureInSeconds() {
		return publishDirectoryStrunctureInSeconds;
	}

	/**
	 * @param publishDirectoryStrunctureInSeconds
	 *            the publishDirectoryStrunctureInSeconds to set
	 */
	public void setPublishDirectoryStrunctureInSeconds(long publishDirectoryStrunctureInSeconds) {
		this.publishDirectoryStrunctureInSeconds = publishDirectoryStrunctureInSeconds;
	}

	class StructureSyncThread extends Thread implements Runnable {

		public StructureSyncThread() {
			super("structureBroadCast");
		}

		public void run() {
			while (running) {

				syncFullDirsAndFiles();

				if (!running) {
					break;
				}

				try {
					Thread.sleep(publishDirectoryStrunctureInSeconds * 1000);
				} catch (final InterruptedException ignored) {
					ignored.printStackTrace();
				}
			}

		}

	}

}
