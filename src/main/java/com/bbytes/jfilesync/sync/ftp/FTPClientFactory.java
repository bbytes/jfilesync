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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class FTPClientFactory {

	private static final Logger log = Logger.getLogger(FTPClientFactory.class);

	private String host;

	private int port;

	private String username;

	private String password;

	public FTPClientFactory() {

	}

	public FTPClient getClientInstance() {

		ExecutorService ftpclientConnThreadPool = Executors.newSingleThreadExecutor();
		Future<FTPClient> future = ftpclientConnThreadPool.submit(new Callable<FTPClient>() {

			FTPClient ftpClient = new FTPClient();

			boolean connected;

			public FTPClient call() throws Exception {

				try {
					while (!connected) {
						try {
							ftpClient.connect(host, port);
							if (!ftpClient.login(username, password)) {
								ftpClient.logout();
							}
							connected = true;
							return ftpClient;
						} catch (Exception e) {
							connected = false;
						}

					}

					int reply = ftpClient.getReplyCode();
					// FTPReply stores a set of constants for FTP reply codes.
					if (!FTPReply.isPositiveCompletion(reply)) {
						ftpClient.disconnect();
					}

					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				return ftpClient;
			}
		});

		FTPClient ftpClient = new FTPClient();
		try {
			System.out.println("Started..");
			ftpClient = future.get(10, TimeUnit.SECONDS);
			System.out.println("Finished!");
		} catch (TimeoutException e) {
			System.out.println("Terminated!");
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
		}

		ftpclientConnThreadPool.shutdownNow();
		return ftpClient;

	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
