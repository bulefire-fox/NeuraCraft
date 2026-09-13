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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@MCP
@Log4j2
public class PlayerFacingBlockQuery extends AbsMCPTool {
    
    @RegisterMCP
    public static void init() {
        log.info("PlayerFacingBlockQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new PlayerFacingBlockQuery());
    }
    
    public PlayerFacingBlockQuery() {
        super(
                "player_facing_block_query",
                "查询玩家当前面向的方块的名称和坐标, 以 (x,y,z,block_name) 的格式返回. 当玩家没有看向任何方块时返回 (none,none,none,air) 代表玩家看向空气",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .name("tool.game.query.player.facing.block")
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
            
            BlockPos blockPos = getPlayerLookingAtBlock(player);
            if (blockPos == null)
                return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(MCPResponse.Result.of("(none,none,none,air)"))
                             .build();
            String blockName = Objects.requireNonNull(server.getLevel(player.level().dimension()))
                                      .getBlockState(blockPos).getBlock().getName().getString();
            return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(
                                     MCPResponse.Result.of("(%d,%d,%d,%s)".formatted(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockName))
                             )
                            .build();
        }
        return MCPMessage.responseBuilder()
                         .id(request.getId())
                         .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "player is not string", null)))
                         .build();
    }
    
    public @Nullable BlockPos getPlayerLookingAtBlock(@NotNull Player player) {
        // 获取玩家眼睛位置
        Vec3 eyePosition = player.getEyePosition(1.0F);
        // 获取玩家视角方向
        Vec3 lookVector = player.getViewVector(1.0F);
        // 计算视线终点（距离玩家5个方块）
        Vec3 reachEnd = eyePosition.add(lookVector.scale(5.0D));
        
        // 创建射线检测
        BlockHitResult result = player.level().clip(new ClipContext(
                eyePosition,
                reachEnd,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
        
        if (result.getType() == BlockHitResult.Type.BLOCK) {
            return result.getBlockPos();
        }
        
        return null; // 没有看向任何方块
    }
}
