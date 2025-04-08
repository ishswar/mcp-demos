package org.story.builder;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class StoryTools {
    private static final Logger log = LoggerFactory.getLogger(StoryTools.class);

    private static final Map<String, Character> characters = new HashMap<>();

    static{
        characters.put("Jack", new Character(
                "Jack is a former spy who now lives as a covert hero.",
                "Invisibility and telepathy"
        ));
        characters.put("Ram", new Character(
                "Ram is an ancient warrior reborn in the modern world to fight for peace.",
                "Invincible body and immense strength"
        ));
        characters.put("Robert", new Character(
                "Robert is a scientist who became part machine after a lab accident.",
                "Power fused with advanced technology"
        ));
    }
    public static McpServerFeatures.SyncToolSpecification getCharacters() throws IOException {
        final String schema = readResourceAsString("empty-input-json-schema.json");
        McpSchema.Tool tool = new McpSchema
                .Tool("list_characters", "list all characters.", schema);
        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, arguments) -> {
                    boolean isError = false;
                    String result;

                    if (characters.isEmpty()) {
                        result = "No characters" ;
                    } else {
                        result = String.join(System.lineSeparator(), characters.keySet());
                    }

                    McpSchema.Content content = new McpSchema.TextContent(result);
                    return new McpSchema.CallToolResult(List.of(content), isError);
                }
        );
    }

    public static McpServerFeatures.SyncToolSpecification getBackstory() throws IOException {
        final String schema = readResourceAsString("character-input-json-schema.json");
        McpSchema.Tool tool = new McpSchema
                .Tool("get_backstory", "get back story.", schema);
        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, arguments) -> {
                    final String character = arguments.get("character").toString();

                    boolean isError = false;
                    String result = Optional.ofNullable(characters.get(character))
                            .map(Character::backstory)
                            .orElse("Character not found.");
                    McpSchema.Content content = new McpSchema.TextContent(result);
                    return new McpSchema.CallToolResult(List.of(content), isError);
                }
        );
    }

    public static McpServerFeatures.SyncToolSpecification getSuperpower() throws IOException {
        final String schema = readResourceAsString("character-input-json-schema.json");
        McpSchema.Tool tool = new McpSchema
                .Tool("get_superpower", "get super power.", schema);
        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, arguments) -> {
                    final String character = arguments.get("character").toString();
                    boolean isError = false;
                    String result = Optional.ofNullable(characters.get(character))
                            .map(Character::superpower)
                            .orElse("Character not found.");
                    McpSchema.Content content = new McpSchema.TextContent(result);
                    return new McpSchema.CallToolResult(List.of(content), isError);
                }
        );
    }

    public static McpServerFeatures.SyncToolSpecification saveStory() throws IOException {
        final String schema = readResourceAsString("title-content-input-json-schema.json");
        McpSchema.Tool tool = new McpSchema
                .Tool("save_story", "save story.", schema);

        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, arguments) -> {
                    final String title = arguments.get("title").toString();
                    final String content = arguments.get("content").toString();

                    String filename = sanitizeFilename(title);
                    String dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                    String fullContent = String.format("# %s\n\n**Date Created:** %s\n\n%s", title, dateCreated, content);
                    boolean isError = false;
                    String result;
                    try {
                        Path path = Paths.get(filename);
                        Files.writeString(path, fullContent);
                        result = "Story has been saved at: " + path.toAbsolutePath();

                    } catch (IOException e) {
                        isError = true;
                        result = e + ": " + e.getMessage();
                        log.error("Error saving story", e);
                        e.printStackTrace(System.err);
                    }

                    McpSchema.Content returnContent = new McpSchema.TextContent(result);
                    return new McpSchema.CallToolResult(List.of(returnContent), isError);
                }
        );


    }

    public static McpServerFeatures.SyncToolSpecification getStory() throws IOException {
        final String schema = readResourceAsString("filename-input-json-schema.json");
        McpSchema.Tool tool = new McpSchema
                .Tool("get_story", "get story.", schema);

        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, arguments) -> {
                    final String filename = arguments.get("filename").toString();

                    boolean isError = false;
                    String result;
                    try {
                        Path path = Paths.get(filename);
                        result = Files.readString(Paths.get(filename));

                    } catch (IOException e) {
                        isError = true;
                        result = e + ": " + e.getMessage();
                        log.error("Error reading story", e);
                        e.printStackTrace(System.err);
                    }

                    McpSchema.Content returnContent = new McpSchema.TextContent(result);
                    return new McpSchema.CallToolResult(List.of(returnContent), isError);
                }
        );
    }


    private static String sanitizeFilename(String title) {
        String filename = title.toLowerCase().replaceAll("\\s+", "_");
        String[] parts = filename.split("_");
        if (parts.length > 4) {
            filename = String.join("_", Arrays.copyOfRange(parts, 0, 4));
        }
        if (filename.length() > 30) {
            filename = filename.substring(0, 30);
        }
        return filename + ".md";
    }

    private static String readResourceAsString(String filename) throws IOException {
        try (InputStream inputStream = StoryTools.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new NoSuchFileException(filename);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(joining(System.lineSeparator()));
        }
    }

    public static void addToServer(McpSyncServer server) {
        try {
            server.addTool(getCharacters());
            server.addTool(getBackstory());
            server.addTool(getSuperpower());
            server.addTool(saveStory());
            server.addTool(getStory());

        } catch (IOException e) {
            log.error("Failed to add tools");
            e.printStackTrace(System.err);
        }
    }

    record Character(String backstory, String superpower) {}
}
