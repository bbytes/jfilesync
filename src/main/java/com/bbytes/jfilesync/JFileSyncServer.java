package com.bbytes.jfilesync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.jgroups.JChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bbytes.jfilesync.sync.JFileSyncListenerThread;

public class JFileSyncServer {

	private static Logger logger = Logger.getLogger(JFileSyncServer.class);

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private JFileSyncListenerThread fileSyncListenerThread;

	private Server server;

	private JChannel fileSyncChannel;

	// private static final String IDE_WAR_LOCATION = "src/main/resources/webapp";

	public void start() {

		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:spring/jfilesync-server.xml");
		fileSyncChannel = appContext.getBean(JChannel.class);
		server = appContext.getBean(Server.class);
		fileSyncListenerThread = new JFileSyncListenerThread(fileSyncChannel, false);

		// add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					shutDown();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});

		try {
			executor.submit(fileSyncListenerThread).get();

			// to server jsp files we need to set this web context else ignore
			// WebAppContext webAppContext = new WebAppContext();
			// webAppContext.setContextPath("/");
			// webAppContext.setWar(IDE_WAR_LOCATION);
			//
			// webAppContext.setServer(server);

			server.start();
			server.join();

			logger.debug("JFile Sync server started....");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void shutDown() throws Exception {
		logger.debug("JFile Sync server shutting down....");
		fileSyncChannel.disconnect();
		fileSyncListenerThread.shutDown();
		executor.shutdown();
		server.stop();
	}
}