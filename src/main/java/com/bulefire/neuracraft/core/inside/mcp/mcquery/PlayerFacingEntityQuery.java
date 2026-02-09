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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@MCP
@Log4j2
public class PlayerFacingEntityQuery extends AbsMCPTool {
    
    @RegisterMCP
    public static void init() {
        log.info("PlayerFacingBlockEntityQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new PlayerFacingEntityQuery());
    }
    
    public PlayerFacingEntityQuery() {
        super(
                "player_facing_entity_query",
                "查询玩家当前面向的实体的名称和坐标, 以 (x,y,z,block_name) 的格式返回. 当玩家没有看向任何方块时返回 (none,none,none,none) 代表玩家没有看向任何实体",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .name("tool.game.query.player.facing.entity")
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
            
            Entity entity = getPlayerLookingAtEntity(player);
            if (entity == null)
                return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(MCPResponse.Result.of("(none,none,none,none)"))
                             .build();
            String entityName = entity.getName().getString();
            return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(
                                     MCPResponse.Result.of("(%f,%f,%f,%s)".formatted(entity.getX(), entity.getY(), entity.getZ(), entityName))
                             )
                            .build();
        }
        return MCPMessage.responseBuilder()
                         .id(request.getId())
                         .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "player is not string", null)))
                         .build();
    }
    
    public @Nullable Entity getPlayerLookingAtEntity(@NotNull Player player) {
        // 获取玩家眼睛位置
        Vec3 eyePosition = player.getEyePosition(1.0F);
        // 获取玩家视角方向
        Vec3 lookVector = player.getViewVector(1.0F);
        // 计算视线终点（距离玩家5个方块）
        Vec3 reachEnd = eyePosition.add(lookVector.scale(5.0D));
        
        List<Entity> entities = player.level().getEntities(
                player,
                player.getBoundingBox().inflate(5.0D),
                entity -> entity != player
        );
        
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Entity entity : entities) {
            // 获取实体的包围盒
            AABB boundingBox = entity.getBoundingBox();
            
            // 计算视线与包围盒的交点
            Optional<Vec3> intersection = boundingBox.clip(eyePosition, reachEnd);
            
            if (intersection.isPresent()) {
                // 计算交点到玩家的距离
                double distance = eyePosition.distanceToSqr(intersection.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }
        
        return closestEntity;
    }
}
