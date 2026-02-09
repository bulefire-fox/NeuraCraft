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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

@MCP
@Log4j2
public class PlayerMainHandItemQuery extends AbsMCPTool {
    
    @RegisterMCP
    public static void init() {
        log.info("PlayerMainHandItemQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new PlayerMainHandItemQuery());
    }
    
    public PlayerMainHandItemQuery() {
        super(
                "player_main_hand_item_query",
                "查询指定玩家主手的物品, 以 (name, namespace_item_id) 返回物品名称和物品命名空间id",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .name("tool.game.query.player.hand.main.item")
                           .params(Map.of("player", new MCPToolInfo.Param("string", "玩家名称", String.class)))
                           .build()
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
        var params = request.getParams();
        if (! params.containsKey("player"))
            return MCPMessage.responseBuilder()
                                .id(request.getId())
                               .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_REQUEST, "player is null", null)))
                               .build();
        if (params.get("player") instanceof String playerName) {
            var server = CUtil.getServer.get();
            var player = server.getPlayerList().getPlayerByName(playerName);
            if (player == null)
                return MCPMessage.responseBuilder()
                                .id(request.getId())
                                .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "player is not in server", null)))
                                .build();
            
            ItemStack itemStack = player.getMainHandItem();
            String itemName = itemStack.getItem().getDescription().getString();
            String itemNamespace = itemStack.getItem().getCreatorModId(itemStack);
            return MCPMessage.responseBuilder()
                            .id(request.getId())
                            .result(MCPResponse.Result.of("(%s, %s)".formatted(itemName, itemNamespace+":"+itemName)))
                            .build();
        }
        
        return MCPMessage.responseBuilder()
                         .id(request.getId())
                         .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "player is not string", null)))
                         .build();
    }
}
