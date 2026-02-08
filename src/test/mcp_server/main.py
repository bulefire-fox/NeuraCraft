from mcp.server.fastmcp import FastMCP

mcp = FastMCP("Demo")


@mcp.tool()
def add(a: int, b: int):
    """ add two numbers """
    return a + b


if __name__ == "__main__":
    mcp.run(transport="streamable-http")
