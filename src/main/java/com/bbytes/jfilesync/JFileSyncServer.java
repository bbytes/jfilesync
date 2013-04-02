package com.bbytes.jfilesync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;

import com.bbytes.jfilesync.sync.JFileSyncListenerServerThread;

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

	private JFileSyncListenerServerThread fileSyncListenerThread;

	private DefaultFtpServer server;

	private JChannel fileSyncChannel;

	private ClassPathXmlApplicationContext context;

	// private static final String IDE_WAR_LOCATION = "src/main/resources/webapp";

	public void start() {

		this.context = new ClassPathXmlApplicationContext(new String[] { "classpath:spring/jfilesync-server.xml" },
				false);
		try {
			this.context.getEnvironment().getPropertySources()
					.addFirst(new ResourcePropertySource("classpath:jfilesync-server.properties"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		this.context.refresh();

		fileSyncChannel = this.context.getBean(JChannel.class);
		server = this.context.getBean(DefaultFtpServer.class);
		fileSyncListenerThread = new JFileSyncListenerServerThread(fileSyncChannel);

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
			// call get() to make the thread finish the Callable call method
			executor.submit(fileSyncListenerThread).get();

			BaseUser user = new BaseUser();
			user.setName((String) this.context.getBean("ftpServerUsername"));
			user.setPassword((String) this.context.getBean("ftpServerPassword"));

			List<Authority> authorities = new ArrayList<Authority>();
			authorities.add(new WritePermission());
			user.setAuthorities(authorities);
			server.getUserManager().save(user);

			server.start();

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
		Object lock = new Object();

		JFileSyncServer jFileSyncServer = new JFileSyncServer();

		try {
			
			String command = "start";

			if (args != null && args.length > 0) {
				command = args[0];
			}

			if (command.equals("start")) {
				logger.info("Starting FTP server daemon");
				logger.info("Type 'stop' to stop the server");
				jFileSyncServer.start();

				synchronized (lock) {
					lock.wait();
				}
			} else if (command.equals("stop")) {
				synchronized (lock) {
					lock.notify();
				}
				logger.info("Stopping FTP server daemon");
				jFileSyncServer.shutDown();
			}
		} catch (Throwable t) {
			logger.error("Daemon error", t);
		}
	}
}