/*
 * Copyright (C) 2013 The Zorba Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.bbytes.jfilesync.sync.ftp;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 * 
 *
 * @author Thanneer
 *
 * @version 
 */
public class JFileSyncFileSystemFactory implements FileSystemFactory{


	private String sourceFolder;
	
	
	/**
	 * @return the sourceFolder
	 */
	public String getSourceFolder() {
		return sourceFolder;
	}


	/**
	 * @param sourceFolder the sourceFolder to set
	 */
	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}


	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FileSystemFactory#createFileSystemView(org.apache.ftpserver.ftplet.User)
	 */
	public FileSystemView createFileSystemView(User user) throws FtpException {
		return new JFileSyncFileSystemView(sourceFolder);
	}

}
