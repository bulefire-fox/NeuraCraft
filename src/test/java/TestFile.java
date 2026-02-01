import com.bulefire.neuracraft.core.agent.Agent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.inside.model.deepseek.DeepSeek;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class TestFile {
    @Test
    void test() {
        Agent agent = new DeepSeek();
        Path path = AgentController.getAgentPath(agent);
    }
}
