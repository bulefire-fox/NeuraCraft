package com.bulefire.neuracraft.core.inside.mcp.mcquery;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.core.mcp.AbsMCPTool;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.annotation.MCP;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;

@MCP
@Log4j2
public class PlayerPositionQuery extends AbsMCPTool {
    @RegisterMCP
    public static void init() {
        log.info("PlayerPositionQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new PlayerPositionQuery());
    }
    
    public PlayerPositionQuery() {
        super(
                "player_position_query",
                "查询当前游戏内指定玩家的位置信息和维度,以 (x,y,z,level) 形式返回",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .host(URI.create(MCPToolInfo.Type.LOCAL.getHead()+"player_position_query"))
                           .method("tool.game.query.player.position")
                           .params(Map.of("player_name",new MCPToolInfo.Param("string","查询的玩家名称", String.class)))
                           .build()
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request) {
        var params = request.getParams();
        if (!params.containsKey("player_name"))
            return MCPMessage.responseFailedBuilder()
                             .id(request.getId())
                             .error(new MCPError(MCPError.INVALID_REQUEST, "player_name is null", null))
                             .build();
        if (params.get("player_name") instanceof String playerName) {
            var server = CUtil.getServer.get();
            Player player = server.getPlayerList().getPlayerByName(playerName);
            if (player == null)
                return MCPMessage.responseFailedBuilder()
                               .id(request.getId())
                               .error(new MCPError(MCPError.INVALID_PARAMS, "player is not in the game", null))
                               .build();
            Position pos = player.position();
            ResourceKey<Level> level = player.level().dimension();
            String levelName;
            if (level == Level.OVERWORLD)
                levelName = "overworld";
            else if (level == Level.NETHER)
                levelName = "nether";
            else if (level == Level.END)
                levelName = "end";
            else
                levelName = "unknow";
            return MCPMessage.responseSuccessBuilder()
                            .id(request.getId())
                            .result(String.format("(%f,%f,%f,%s)", pos.x(), pos.y(), pos.z(), levelName))
                            .build();
        }
        return MCPMessage.responseFailedBuilder()
                         .id(request.getId())
                         .error(new MCPError(MCPError.INVALID_PARAMS, "player_name is not a string", null))
                         .build();
    }
}
