package story_builder.story_builder_server;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StoryBookService {
    private static final Logger log = LoggerFactory.getLogger(StoryBookService.class);
    private final Map<String, Character> characters = new HashMap<>();

    @Tool(name = "get_characters", description = "Get the list of all available character names")
    public List<String> getCharacters() {
        return new ArrayList<>(characters.keySet());
    }

    @Tool(name = "get_backstory", description = "Get the backstory of a specified character")
    public String getBackstory(String character) {
        return Optional.ofNullable(characters.get(character))
                .map(Character::backstory)
                .orElse("Character not found.");
    }

    @Tool(name = "get_superpower", description = "Get the superpower of a specified character")
    public String getSuperpower(String character) {
        return Optional.ofNullable(characters.get(character))
                .map(Character::superpower)
                .orElse("Character not found.");
    }

    @Tool(name = "save_story", description = "Save a story to a markdown file with title and creation date")
    public String saveStory(String title, String content) {
        try {
            String filename = sanitizeFilename(title);
            String dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            String fullContent = String.format("# %s\n\n**Date Created:** %s\n\n%s", title, dateCreated, content);
            
            Path path = Paths.get(filename);
            Files.writeString(path, fullContent);
            return "Story has been saved at: " + path.toAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving story", e);
            return "Error saving story: " + e.getMessage();
        }
    }

    @Tool(name = "list_stories", description = "List all saved story files in markdown format")
    public List<String> listStories() {
        try {
            return Files.list(Paths.get("."))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".md"))
                    .toList();
        } catch (IOException e) {
            log.error("Error listing stories", e);
            return Collections.emptyList();
        }
    }

    @Tool(name = "get_story", description = "Read the content of a specific story file")
    public String getStory(String filename) {
        try {
            return Files.readString(Paths.get(filename));
        } catch (IOException e) {
            log.error("Error reading story", e);
            return "Story file not found.";
        }
    }



    private String sanitizeFilename(String title) {
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

    @PostConstruct
    public void init() {
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

    record Character(String backstory, String superpower) {}
}
