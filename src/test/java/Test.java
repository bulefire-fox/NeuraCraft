import com.bulefire.neuracraft.core.mcp.entity.AgentInput;
import com.google.gson.Gson;

public class Test {
    public static void main(String[] args) {
        String s = """
                  {
                  "tool_call": {
                    "id": "此处填写与工具列表中完全一致的唯一调用ID",
                    "parameters": {
                      "参数1名称": "参数1值",
                      "参数2名称": "参数2值"
                    }
                  }
                }
                """;
        AgentInput agentInput = new Gson().fromJson(s, AgentInput.class);
        System.out.println(agentInput.toString());
    }
}
