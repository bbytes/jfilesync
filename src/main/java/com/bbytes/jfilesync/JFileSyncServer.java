package com.bbytes.jfilesync;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.jgroups.stack.GossipRouter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JFileSyncServer {

	private static Logger logger = Logger.getLogger(JFileSyncServer.class);

//	private static final String IDE_WAR_LOCATION = "src/main/resources/webapp";

	public static void main(String[] args) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:spring/jetty-server.xml");

		GossipRouter gossipRouter = appContext.getBean(GossipRouter.class);
		Server server = appContext.getBean(Server.class);

		try {

			gossipRouter.start();
			
			// to server jsp files we need to set this web context else ignore
//			WebAppContext webAppContext = new WebAppContext();
//			webAppContext.setContextPath("/");
//			webAppContext.setWar(IDE_WAR_LOCATION);
//
//			webAppContext.setServer(server);
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
						gossipRouter.stop();
						server.stop();
						System.exit(0);
					}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			logger.error("Error when starting", e);
		}
	}
}