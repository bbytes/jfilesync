package com.bbytes.jfilesync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bbytes.jfilesync.sync.JFileSyncListenerThread;

public class JFileSyncServer {

	private static Logger logger = Logger.getLogger(JFileSyncServer.class);

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private static JFileSyncListenerThread fileSyncListenerThread;

	private static Server server;

	private static JChannel fileSyncChannel;

	// private static final String IDE_WAR_LOCATION = "src/main/resources/webapp";

	public static void main(String[] args) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:spring/jfilesync-server.xml");
		fileSyncChannel = appContext.getBean(JChannel.class);
		server = appContext.getBean(Server.class);
		fileSyncListenerThread = new JFileSyncListenerThread(fileSyncChannel, false);
		try {
			boolean status = executor.submit(fileSyncListenerThread).get();

			// to server jsp files we need to set this web context else ignore
			// WebAppContext webAppContext = new WebAppContext();
			// webAppContext.setContextPath("/");
			// webAppContext.setWar(IDE_WAR_LOCATION);
			//
			// webAppContext.setServer(server);

			server.start();
			server.join();

			System.out.println("Zorba web server running....");
			System.out.println("Enter :q and hit enter to quit: ");
			while (true) {

				BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
				String str = bufferRead.readLine();
				if (str != null && !str.isEmpty())
					if (str.trim().equals(":q")) {
						System.out.println("exiting system...");
						shutDown();
						break;
					}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			logger.error("Error when starting", e);
		}
	}

	public static void shutDown() throws Exception {
		fileSyncChannel.disconnect();
		fileSyncListenerThread.shutDown();
		executor.shutdown();
		server.stop();
		System.exit(0);
	}
}