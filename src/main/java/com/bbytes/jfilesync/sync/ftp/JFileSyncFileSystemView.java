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
package com.bbytes.jfilesync.sync.ftp;

import java.io.File;

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncFileSystemView implements FileSystemView {

	private final Logger LOG = LoggerFactory.getLogger(NativeFileSystemView.class);

	// the root directory will always end with '/'.
	private String rootDir;

	// the first and the last character will always be '/'
	// It is always with respect to the root directory.
	private String currDir;

	private User user;

	// private boolean writePermission;

	private boolean caseInsensitive = false;

	public JFileSyncFileSystemView(String sourceDirectory) {
		this.rootDir = new File(sourceDirectory).getPath()+"/";
		this.currDir = "/";
	}

	/**
	 * Get the user home directory. It would be the file system root for the user.
	 */
	public FtpFile getHomeDirectory() {
		return new NativeFtpFile("/", new File(rootDir), user);
	}

	/**
	 * Get the current directory.
	 */
	public FtpFile getWorkingDirectory() {
		FtpFile fileObj = null;

		File file = new File(rootDir, currDir.substring(1));
		fileObj = new NativeFtpFile(currDir, file, user);

		return fileObj;
	}

	/**
	 * Get file object.
	 */
	public FtpFile getFile(String file) {

		// get actual file object
		String physicalName = NativeFtpFile.getPhysicalName(rootDir, currDir, file, caseInsensitive);
		File fileObj = new File(physicalName);

		// strip the root directory and return
		String userFileName = physicalName.substring(rootDir.length() - 1);
		return new NativeFtpFile(userFileName, fileObj, user);
	}

	/**
	 * Change directory.
	 */
	public boolean changeWorkingDirectory(String dir) {

		// not a directory - return false
		dir = NativeFtpFile.getPhysicalName(rootDir, currDir, dir, caseInsensitive);
		File dirObj = new File(dir);
		if (!dirObj.isDirectory()) {
			return false;
		}

		// strip user root and add last '/' if necessary
		dir = dir.substring(rootDir.length() - 1);
		if (dir.charAt(dir.length() - 1) != '/') {
			dir = dir + '/';
		}

		currDir = dir;
		return true;
	}

	/**
	 * Is the file content random accessible?
	 */
	public boolean isRandomAccessible() {
		return true;
	}

	/**
	 * Dispose file system view - does nothing.
	 */
	public void dispose() {
	}

}
