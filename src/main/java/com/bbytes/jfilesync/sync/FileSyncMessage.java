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

import java.io.Serializable;
import java.net.URL;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FileSyncMessage implements Serializable{

	private static final long serialVersionUID = 4973747277121428372L;

	private FileMessageType fileMessageType;

	private URL fileUrl;

	private String fileName;

	private boolean isDirectory = false;

	public FileSyncMessage(FileMessageType fileMessageType, URL fileUrl, String fileName) {
		this.fileMessageType = fileMessageType;
		this.fileUrl = fileUrl;
		this.fileName = fileName;
	}

	/**
	 * @return the fileMessageType
	 */
	public FileMessageType getFileMessageType() {
		return fileMessageType;
	}

	/**
	 * @param fileMessageType
	 *            the fileMessageType to set
	 */
	public void setFileMessageType(FileMessageType fileMessageType) {
		this.fileMessageType = fileMessageType;
	}

	/**
	 * @return the fileUrl
	 */
	public URL getFileUrl() {
		return fileUrl;
	}

	/**
	 * @param fileUrl
	 *            the fileUrl to set
	 */
	public void setFileUrl(URL fileUrl) {
		this.fileUrl = fileUrl;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the isDirectory
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * @param isDirectory
	 *            the isDirectory to set
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	
	public String toString(){
		return "["+ fileMessageType.toString()+"] : " + fileUrl   ;
	}
}
