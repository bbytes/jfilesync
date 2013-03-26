package com.bbytes.jfilesync;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.jgroups.JChannel;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;

import com.bbytes.jfilesync.sync.JFileSyncListenerThread;

/**
 * The file sync server which sends out file modification messages to client based on modification
 * is source folder
 * 
 * 
 * @author Thanneer
 * 
 * @version
 */
public class JFileSyncServer {

	private static Logger logger = Logger.getLogger(JFileSyncServer.class);

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private JFileSyncListenerThread fileSyncListenerThread;

	private Server server;

	private JChannel fileSyncChannel;

	private ClassPathXmlApplicationContext context;

	// private static final String IDE_WAR_LOCATION = "src/main/resources/webapp";

	public void start() {

		this.context = new ClassPathXmlApplicationContext(new String[] {"classpath:spring/jfilesync-server.xml" },
				false);
		try {
			this.context.getEnvironment().getPropertySources()
					.addFirst(new ResourcePropertySource("classpath:jfilesync-server.properties"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		this.context.refresh();

		fileSyncChannel = this.context.getBean(JChannel.class);
		server = this.context.getBean(Server.class);
		fileSyncListenerThread = new JFileSyncListenerThread(fileSyncChannel, false);
		fileSyncListenerThread.setMode("server");

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

	/**
	 * Close all resource in server
	 */
	public void shutDown() throws Exception {
		logger.debug("JFile Sync server shutting down....");
		fileSyncChannel.disconnect();
		fileSyncListenerThread.shutDown();
		executor.shutdown();
		server.stop();
		this.context.destroy();
	}

	public static void main(String[] args) {
		JFileSyncServer jFileSyncServer = new JFileSyncServer();
		jFileSyncServer.start();
	}
}