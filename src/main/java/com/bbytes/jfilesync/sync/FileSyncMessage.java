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
import java.net.URI;

/**
 * The file events are sent as file sync messages to client nodes
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FileSyncMessage implements Serializable {

	private static final long serialVersionUID = 4973747277121428372L;

	private FileMessageType fileMessageType;

	private URI fileUrl;

	private String baseFolderRelativePath;

	private String fileName;

	private boolean isDirectory = false;

	private String originalFilePath = "";

	public FileSyncMessage(FileMessageType fileMessageType, URI fileUrl, String fileName) {
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
	public URI getFileUrl() {
		return fileUrl;
	}

	/**
	 * @param fileUrl
	 *            the fileUrl to set
	 */
	public void setFileUrl(URI fileUrl) {
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

	/**
	 * The file path from base folder for the file .The directory structure for that file in base
	 * folder For eg : /home/bbytes is the base folder and a file is inside
	 * /home/bbytes/folder1/subfolder2/help.txt then baseFolderRelativePath value is
	 * /folder1/subfolder2/
	 * 
	 * @return the baseFolderRelativePath
	 */
	public String getBaseFolderRelativePath() {
		return baseFolderRelativePath;
	}

	/**
	 * The file path from base folder for the file .The directory structure for that file in base
	 * folder
	 * 
	 * @param baseFolderRelativePath
	 *            the baseFolderRelativePath to set
	 */
	public void setBaseFolderRelativePath(String baseFolderRelativePath) {
		this.baseFolderRelativePath = baseFolderRelativePath;
	}

	/**
	 * @return the originalFilePath
	 */
	public String getOriginalFilePath() {
		return originalFilePath;
	}

	/**
	 * @param originalFilePath
	 *            the originalFilePath to set
	 */
	public void setOriginalFilePath(String originalFilePath) {
		this.originalFilePath = originalFilePath;
	}

	public String toString() {
		return "IsDirectory : " + isDirectory + "  [" + fileMessageType.toString() + "] : " + originalFilePath
				+ "[ BaseFolderRelativePath ] : " + baseFolderRelativePath;
	}
}
