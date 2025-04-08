package org.story.builder;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class StoryPrompts {


    public static McpServerFeatures.SyncPromptSpecification getCharacters() throws IOException {
        McpSchema.Prompt prompt = new McpSchema
                .Prompt("get_characters", "Get all characters.", List.of());
        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();
                    McpSchema.TextContent content = new McpSchema.TextContent(
                            String.format("What are the characters of this story")
                    );
                    McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);
                    return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
                }
        );
    }

    public static McpServerFeatures.SyncPromptSpecification getBackstory() throws IOException {
        McpSchema.PromptArgument character = new McpSchema
                .PromptArgument("character", "The back story character", true);
        McpSchema.Prompt prompt = new McpSchema
                .Prompt("get_backstory", "Get back story character.", List.of(character));
        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();
                    McpSchema.TextContent content = new McpSchema.TextContent(
                            String.format("What is the back story character of this story: %s", arguments.get("character"))
                    );
                    McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);
                    return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
                }
        );

    }

    public static McpServerFeatures.SyncPromptSpecification getSuperpower() throws IOException {
        McpSchema.PromptArgument character = new McpSchema
                .PromptArgument("character", "The super power character", true);
        McpSchema.Prompt prompt = new McpSchema
                .Prompt("get_superpower", "Get super power.", List.of(character));
        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();
                    McpSchema.TextContent content = new McpSchema.TextContent(
                            String.format("What is the super power of this story: %s", arguments.get("character"))
                    );
                    McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);
                    return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
                }
        );

    }

    public static McpServerFeatures.SyncPromptSpecification saveStory() throws IOException {
        McpSchema.PromptArgument title = new McpSchema
                .PromptArgument("title", "The title of story", true);
        McpSchema.PromptArgument storyContent = new McpSchema
                .PromptArgument("content", "The content of story", true);


        List<McpSchema.PromptArgument> args = List.of(title, storyContent);
        McpSchema.Prompt prompt = new McpSchema
                .Prompt("save_story", "Save story to disck.", args);

        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();

                    StringBuilder promptMessage = new StringBuilder("Please save the story to disk: ");
                    promptMessage.append(arguments.get("title"));
                    promptMessage.append(arguments.get("content"));

                    McpSchema.TextContent content = new McpSchema.TextContent(promptMessage.toString());
                    McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);
                    return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
                }
        );

    }

    public static McpServerFeatures.SyncPromptSpecification getStory() throws IOException {
        McpSchema.PromptArgument filename = new McpSchema
                .PromptArgument("filename", "The file to read", true);

        McpSchema.Prompt prompt = new McpSchema
                .Prompt("get_story", "Read complete story.", List.of(filename));

        return new McpServerFeatures.SyncPromptSpecification(
                prompt,
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();
                    McpSchema.TextContent content = new McpSchema.TextContent(
                            String.format("What is the content of this story: %s", arguments.get("filename"))
                    );
                    McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);
                    return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
                }
        );
    }

    public static void addToServer(McpSyncServer server) throws IOException {
        server.addPrompt(getCharacters());
        server.addPrompt(getBackstory());
        server.addPrompt(getSuperpower());
        server.addPrompt(saveStory());
        server.addPrompt(getStory());
    }
}
