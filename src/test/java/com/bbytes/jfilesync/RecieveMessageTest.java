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

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/jfilesync-client.xml" })
public class RecieveMessageTest {

	@Autowired
	JChannel channel;

	@Test
	public void testSend() throws  InterruptedException {
		channel.setReceiver(new ReceiverAdapter() {
			public void receive(Message msg) {
				System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
			}
			
			public void viewAccepted(View view) {
				System.out.println("new client joined " +view.toString());
				
		    }
			
		});
		Thread.currentThread().sleep(2000);
		channel.close();
	}
}
