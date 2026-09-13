from mcp.server.fastmcp import FastMCP
from typing import Annotated

mcp = FastMCP("Demo", port=8001)


@mcp.tool(

)
def add_sse(a: Annotated[int, "a number"], b: int) -> int:
    """
    add two numbers

    Args:
        a (int) a number
        b (int) a number

    Returns:
          the sum of two number

    """
    return a + b


if __name__ == "__main__":
    mcp.run(transport="sse")
