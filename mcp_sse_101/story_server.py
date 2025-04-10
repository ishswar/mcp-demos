import os
from datetime import datetime

from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.prompts import base

mcp = FastMCP("StoryServer", description="A tool for creating and saving stories with characters.",
              port=8083)

CHARACTERS = {
    "Jack": {
        "backstory": "Jack is a former spy who now lives as a covert hero.",
        "superpower": "Invisibility and telepathy"
    },
    "Ram": {
        "backstory": "Ram is an ancient warrior reborn in the modern world to fight for peace.",
        "superpower": "Invincible body and immense strength"
    },
    "Robert": {
        "backstory": "Robert is a scientist who became part machine after a lab accident.",
        "superpower": "Power fused with advanced technology"
    }
}


@mcp.tool(description="Get the list of all available character names.")
def get_characters(reason: str) -> list[str]:
    """Returns a list of all defined character names.

    Args:
        reason (str): The reason for fetching the character names.

    Returns:
        list[str]: A list of character names.
    """
    return list(CHARACTERS.keys())


@mcp.tool(description="Get the backstory of a specified character.")
def get_backstory(character: str) -> str:
    """Returns the backstory of the given character.

    Args:
        character (str): The name of the character.

    Returns:
        str: The character's backstory or a not-found message.
    """
    return CHARACTERS.get(character, {}).get("backstory", "Character not found.")


@mcp.tool(description="Get the superpower of a specified character.")
def get_superpower(character: str) -> str:
    """Returns the superpower of the given character.

    Args:
        character (str): The name of the character.

    Returns:
        str: The character's superpower or a not-found message.
    """
    return CHARACTERS.get(character, {}).get("superpower", "Character not found.")


def get_current_date() -> str:
    return datetime.now().strftime("%B %d, %Y")


def sanitize_filename(title: str) -> str:
    filename = title.replace(' ', '_').lower()
    if filename.count('_') > 3:
        parts = filename.split('_')[:4]
        filename = '_'.join(parts)
    if len(filename) > 30:
        filename = filename[:30]
    return filename + ".md"


@mcp.tool(description="Save a story to a markdown file with title and creation date.")
def save_story(title: str, content: str) -> str:
    """Saves a story in markdown format using the title as filename and adds the date created.

    Args:
        title (str): The title of the story.
        content (str): The story content in markdown format.

    Returns:
        str: Absolute path where the story file was saved.
    """
    filename = sanitize_filename(title)
    date_created = get_current_date()
    with open(filename, "w", encoding="utf-8") as f:
        f.write(f"# {title}\n\n")
        f.write(f"**Date Created:** {date_created}\n\n")
        f.write(content)
    return f"Story has been saved at: {os.path.abspath(filename)}"


@mcp.tool(description="List all saved story files in markdown format.")
def list_stories(reason: str) -> list[str]:
    """Returns a list of all saved .md story filenames in the current directory.

    Args:
        reason (str): The reason for listing the story files.

    Returns:
        list[str]: List of markdown file names.
    """
    files = [f for f in os.listdir('.') if f.endswith('.md')]
    return files


@mcp.tool(description="Read the content of a specific story file.")
def get_story(filename: str) -> str:
    """Reads and returns the contents of the specified markdown story file.

    Args:
        filename (str): The name of the markdown file.

    Returns:
        str: Contents of the file or an error message if not found.
    """
    if not os.path.exists(filename):
        return "Story file not found."
    with open(filename, "r", encoding="utf-8") as f:
        return f.read()


@mcp.prompt("story-creator")
def story_creator_prompt(user_query: str) -> list[base.Message]:
    return [
        base.UserMessage(
            f"""
            Based on the above prompt, create a beautiful story that includes the traits and backstories of each character.
            Provide the story in markdown format with an appropriate title.
            """
        )
    ]


if __name__ == "__main__":
    print("MCP Story Server is running using SSE transport ...")
    mcp.run(transport="sse")
