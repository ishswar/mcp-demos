package org.story.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SseServer {

    private static final Logger logger = LoggerFactory.getLogger(SseServer.class);
    private static final String SERVER_NAME = "mcp-server-story-builder";
    private static final String SERVER_VERSION = "0.8.1";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String MSG_ENDPOINT = "/message";
    private static final String SSE_ENDPOINT = "/sse";
    private McpSyncServer server;

    private void initialize() throws IOException {
        McpSchema.ServerCapabilities serverCapabilities = McpSchema.ServerCapabilities.builder()
            .tools(true)
            .prompts(true)
            .resources(true, true)
            .build();

        HttpServletSseServerTransportProvider transport = new HttpServletSseServerTransportProvider(
            JSON, MSG_ENDPOINT, SSE_ENDPOINT
        );
        server = McpServer.sync(transport)
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(serverCapabilities)
            .build();

        StoryPrompts.addToServer(server);
        StoryTools.addToServer(server);

        startHttpServer(transport);
    }

    private void startHttpServer(HttpServletSseServerTransportProvider transport) {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(transport);
        servletContextHandler.addServlet(servletHolder, "/*");

        // Start server on 0.0.0.0:8282
        Server httpserver = new Server(new InetSocketAddress("0.0.0.0", 8282));
        httpserver.setHandler(servletContextHandler);

        try {
            httpserver.start();
            logger.info("Jetty HTTP server started on http://0.0.0.0:8282");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutting down HTTP server");
                    httpserver.stop();
                    server.close();
                } catch (Exception e) {
                    logger.error("Error stopping HTTP server", e);
                }
            }));

            httpserver.join();
        } catch (Exception e) {
            logger.error("Error starting HTTP server on http://0.0.0.0:8282", e);
            server.close();
        }
    }

    public static void main(String[] args) throws IOException {
        SseServer mcpSseServer = new SseServer();
        mcpSseServer.initialize();
    }

}
