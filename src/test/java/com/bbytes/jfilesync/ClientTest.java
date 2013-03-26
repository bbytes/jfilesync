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
package com.bbytes.jfilesync;

import org.junit.Test;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class ClientTest {

	/**
	 * Test client start and stop 
	 * @throws Exception
	 */
	@Test
	public void testClient() throws Exception {
		JFileSyncClient client = new JFileSyncClient();
		client.start();
		Thread.currentThread().sleep(200000);
		client.shutDown();
	}
	
	
}
