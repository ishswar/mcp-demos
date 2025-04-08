package story_builder.story_builder_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.*;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;

@SpringBootApplication
public class StoryBuilderServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryBuilderServerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> danTools(StoryBookService storyBookService) {
		return List.of(ToolCallbacks.from(storyBookService));
	}

	@Bean
	public List<McpServerFeatures.SyncPromptRegistration> myPrompts() {
		var prompt = new McpSchema.Prompt("greeting", "A friendly greeting prompt",
			List.of(new McpSchema.PromptArgument("name", "The name to greet", true)));

		var promptRegistration = new McpServerFeatures.SyncPromptRegistration(prompt, getPromptRequest -> {
			String nameArgument = (String) getPromptRequest.arguments().get("name");
			if (nameArgument == null) { nameArgument = "friend"; }
			var userMessage = new PromptMessage(Role.USER, new TextContent("Hello " + nameArgument + "! How can I assist you today?"));
			return new GetPromptResult("A personalized greeting message", List.of(userMessage));
		});

		return List.of(promptRegistration);
	}

}
