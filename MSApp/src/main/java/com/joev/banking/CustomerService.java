package com.joev.banking;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Run the Customer REST service listening on a specific port
 */
public class CustomerService {
	private static final Logger LOG = Log.getLogger(CustomerService.class);

	// TODO: Provide a way to specify this dynamically
	private static final int PORT = 8000;

	static Server createServer(int port) {
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");

		Server server = new Server(port);
		server.setHandler(servletContextHandler);

		ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
		servletHolder.setInitOrder(0);

		// Tell the Jersey Servlet which REST service/classes to load
		servletHolder.setInitParameter("com.sun.jersey.config.property.packages", "com.joev.banking");

		// Tell the Jersey Servlet to use the POJO mapping feature
		servletHolder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

		return server;
	}

	public static void main(String[] args) throws Exception {
		LOG.info("START");
		Server server = createServer(PORT);
		// Start the server and run it forever...
		try {
			LOG.info("Starting server...");
			server.start();
			LOG.info("Joining server...");
			server.join();
		} finally {
			LOG.info("Destroying server...");
			server.destroy();
		}
		LOG.info("END");
	}

}
