from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.prompts import base
import os
from datetime import datetime
import re

mcp = FastMCP("StoryServer", description="A tool for creating and saving stories with characters.",
              port=8080)

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

@mcp.tool()
def get_characters() -> list[str]:
    return list(CHARACTERS.keys())


@mcp.tool()
def get_backstory(character: str) -> str:
    return CHARACTERS.get(character, {}).get("backstory", "Character not found.")


@mcp.tool()
def get_superpower(character: str) -> str:
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


@mcp.tool()
def save_story(title: str, content: str) -> str:
    filename = sanitize_filename(title)
    date_created = get_current_date()
    with open(filename, "w", encoding="utf-8") as f:
        f.write(f"# {title}\n\n")
        f.write(f"**Date Created:** {date_created}\n\n")
        f.write(content)
    return f"Story has been saved at: {os.path.abspath(filename)}"


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
    print("MCP Story Server is running using stdio transport ...")
    mcp.run(transport="sse")
