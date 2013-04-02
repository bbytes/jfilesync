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

import java.util.Collection;

/**
 * The file events are sent as file sync messages to client nodes. The dir file list sent with
 * checksum to find if any modification
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FullDirSyncMessage extends FileSyncMessage {

	private static final long serialVersionUID = 4032122039061595484L;

	private Collection<FileSyncMessage> listOfFilesAndDir;

	/**
	 * @param fileMessageType
	 * @param fileUrl
	 * @param fileName
	 */
	public FullDirSyncMessage(FileMessageType fileMessageType, String fileName) {
		super(fileMessageType, fileName);
	}

	/**
	 * @return the listOfFilesAndDir
	 */
	public Collection<FileSyncMessage> getListOfFilesAndDir() {
		return listOfFilesAndDir;
	}

	/**
	 * @param listOfFilesAndDir
	 *            the listOfFilesAndDir to set
	 */
	public void setListOfFilesAndDir(Collection<FileSyncMessage> listOfFilesAndDir) {
		this.listOfFilesAndDir = listOfFilesAndDir;
	}

}
