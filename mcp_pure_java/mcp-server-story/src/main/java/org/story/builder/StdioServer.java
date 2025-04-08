package org.story.builder;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.IOException;

public class StdioServer {

    private static final String SERVER_NAME = "mcp-server-story-builder";

    private static final String SERVER_VERSION = "0.8.1";

    private McpSyncServer server;

    private void initialize() {
        McpSchema.ServerCapabilities serverCapabilities = McpSchema.ServerCapabilities.builder()
            .tools(true)
            .prompts(true)
            .resources(true, true)
            .build();

        server = McpServer.sync(new StdioServerTransportProvider())
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(serverCapabilities)
            .build();

        System.err.println(SERVER_NAME + " " + SERVER_VERSION + " initialized in STDIO mode");
    }

    public static void main(String[] args) throws IOException {
        StdioServer mcpStdioServer = new StdioServer();
        mcpStdioServer.initialize();
        //McpResources.addAllTo(mcpStdioServer.server);
        StoryPrompts.addToServer(mcpStdioServer.server);
        StoryTools.addToServer(mcpStdioServer.server);
    }

}
