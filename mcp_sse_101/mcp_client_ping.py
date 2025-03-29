import asyncio
import argparse
from mcp import ClientSession
from mcp.client.sse import sse_client


async def interactive_client(url: str):
    print(f"\nConnecting to MCP server at: {url} ...")

    try:
        async with sse_client(url=url) as streams:
            async with ClientSession(*streams) as session:
                await session.initialize()

                try:
                    await session.send_ping()
                    print("✅ Connection successful! Ping sent.")
                except Exception as e:
                    print(f"❌ Error sending ping: {e}")
                    return

                # Fetch tools and prompts once at start
                tools = await session.list_tools()
                prompts = await session.list_prompts()

                tool_names = [tool.name for tool in tools.tools]
                prompt_names = [prompt.name for prompt in prompts.prompts]

                print(f"\n✅ Server is ready. Available tools: {', '.join(tool_names)}")
                print(f"Available prompts: {', '.join(prompt_names)}")
                print("\nYou can now continue to interact with the server...")

    except Exception as e:
        print("❌ Error connecting to server:")
        if hasattr(e, 'exceptions'):  # ExceptionGroup
            for sub_e in e.exceptions:
                print(f" - {type(sub_e).__name__}: {sub_e}")
        else:
            print(f" - {type(e).__name__}: {e}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Simple MCP Client to list tools and prompts")
    parser.add_argument(
        "--url",
        type=str,
        required=True,
        help="MCP SSE server URL (e.g. https://your-server-name.saci.r.killercoda.com/sse)"
    )

    args = parser.parse_args()
    asyncio.run(interactive_client(url=args.url))
