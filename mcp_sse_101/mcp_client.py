import asyncio
import mcp
from mcp import ClientSession
from mcp.client.sse import sse_client
from importlib.metadata import version, PackageNotFoundError

try:
    mcp_version = version('mcp')
    print(f"mcp version: {mcp_version}")
except PackageNotFoundError:
    print("mcp package is not installed.")


async def interactive_client():
    # Prompt user for SSE URL
    url = input(
        "Enter your MCP SSE server URL (e.g. https://abc-10-244-24-75-8080.saci.r.killercoda.com/sse): ").strip()

    if not url:
        print("❌ Error: URL is required to connect to the server.")
        return

    print(f"\nConnecting to MCP server via SSE at: {url} ...")

    try:
        async with sse_client(url=url) as streams:
            async with ClientSession(*streams) as session:
                await session.initialize()

                # Fetch tools and prompts once at start
                tools = await session.list_tools()
                prompts = await session.list_prompts()

                tool_names = [tool.name for tool in tools.tools]
                prompt_names = [prompt.name for prompt in prompts.prompts]

                print("\n📦 Welcome to the MCP Client!")
                print("You can list tools, run tools, call prompts, or exit.")
                print("Type `help` for options.\n")

                while True:
                    command = input(">>> ").strip().lower()

                    if command == "help":
                        print("""
Available commands:
- tools     : List all available tools with descriptions
- prompts   : List all prompts
- call-tool : Call a tool by name
- prompt    : Call a prompt
- exit      : Exit the client
                        """)

                    elif command == "tools":
                        print("\nAvailable Tools:")
                        for tool in tools.tools:
                            print(f"\nTool: {tool.name}")
                            print(f"Description: {tool.description}")
                            input_schema = tool.inputSchema or {}
                            properties = input_schema.get("properties", {})
                            required_args = set(input_schema.get("required", []))
                            if not properties:
                                print("  Input: None")
                            else:
                                print("  Input Parameters:")
                                for arg_name, arg_info in properties.items():
                                    arg_type = arg_info.get("type", "unknown")
                                    required = "Yes" if arg_name in required_args else "No"
                                    print(f"    - {arg_name} (Type: {arg_type}, Required: {required})")
                        print()

                    elif command == "prompts":
                        print("\nAvailable Prompts:")
                        for prompt in prompts.prompts:
                            print(f"\nPrompt: {prompt.name}")
                            print(f"Description: {prompt.description or 'No description provided.'}")
                            if not prompt.arguments:
                                print("  Input: None")
                            else:
                                print("  Input Parameters:")
                                for arg in prompt.arguments:
                                    arg_name = arg.name
                                    required = "Yes" if arg.required else "No"
                                    desc = arg.description or "No description"
                                    print(f"    - {arg_name} (Required: {required}) — {desc}")
                        print()

                    elif command == "call-tool":
                        print(f"Available tools: {', '.join(tool_names)}")
                        tool_name = input("Enter tool name: ").strip()

                        # Find the tool definition to extract its schema
                        tool_def = next((t for t in tools.tools if t.name == tool_name), None)
                        if not tool_def:
                            print(f"Tool '{tool_name}' not found.")
                            return

                        tool_args = {}
                        input_schema = tool_def.inputSchema or {}
                        properties = input_schema.get("properties", {})
                        required_args = set(input_schema.get("required", []))

                        for arg_name, arg_info in properties.items():
                            arg_type = arg_info.get("type", "string")
                            prompt = f"Enter value for '{arg_name}' ({arg_type})"
                            if arg_name in required_args:
                                prompt += " [required]"
                            prompt += ": "
                            value = input(prompt).strip()
                            tool_args[arg_name] = value

                        try:
                            result = await session.call_tool(tool_name, arguments=tool_args)
                            print("\nTool Output:")
                            for content in result.content:
                                print(content.text)
                        except Exception as e:
                            print(f"Error calling tool: {e}")

                    elif command == "prompt":
                        print(f"Available prompts: {', '.join(prompt_names)}")
                        prompt_name = input("Enter prompt name: ").strip()
                        user_input = input("Enter user query: ")
                        try:
                            result = await session.get_prompt(prompt_name, arguments={"user_query": user_input})
                            print("\nPrompt Output:")
                            for message in result.messages:
                                content = message.content
                                role = message.role
                                if hasattr(content, "text"):
                                    print(f"[{role}] {content.text.strip()}")
                        except Exception as e:
                            print(f"Error calling prompt: {e}")

                    elif command == "exit":
                        print("Goodbye!")
                        break

                    else:
                        print("Unknown command. Type `help` to see available commands.")

    except Exception as e:
        print("❌ Error connecting to server:")
        if hasattr(e, 'exceptions'):  # ExceptionGroup
            for sub_e in e.exceptions:
                print(f" - {type(sub_e).__name__}: {sub_e}")
        else:
            print(f" - {type(e).__name__}: {e}")


if __name__ == "__main__":
    print("Starting the MCP Client ...")
    asyncio.run(interactive_client())
