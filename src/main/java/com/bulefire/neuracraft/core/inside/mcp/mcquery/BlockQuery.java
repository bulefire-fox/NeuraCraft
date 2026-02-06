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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

@MCP
@Log4j2
public class BlockQuery extends AbsMCPTool {
    
    @RegisterMCP
    public static void init() {
        log.info("BlockQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new BlockQuery());
    }
    
    // Minecraft坐标范围限制
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;
    private static final int MIN_XZ = -30000000;
    private static final int MAX_XZ = 30000000;
    
    public BlockQuery() {
        super(
                "block_query",
                "查询指定坐标的方块名称",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .host(URI.create(MCPToolInfo.Type.LOCAL.getHead() + "block_query"))
                           .method("tool.game.query.position.block")
                           .params(Map.of(
                                           "x", new MCPToolInfo.Param("int", "方块X坐标", Integer.class),
                                           "y", new MCPToolInfo.Param("int", "方块Y坐标", Integer.class),
                                           "z", new MCPToolInfo.Param("int", "方块Z坐标", Integer.class),
                                           "level", new MCPToolInfo.Param("string", "维度名称, 在[overworld,nether,end]之中", String.class)
                                   )
                           )
                           .build()
        
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
        var params = request.getParams();
        if (! params.containsKey("x") ||
                ! params.containsKey("y") ||
                ! params.containsKey("z") ||
                ! params.containsKey("level"))
            
            return MCPMessage.responseFailedBuilder()
                             .id(request.getId())
                             .error(new MCPError(MCPError.INVALID_REQUEST, "x or y or z or level is null", null))
                             .build();
        if ((params.get("x") instanceof Integer x) &&
                (params.get("y") instanceof Integer y) &&
                (params.get("z") instanceof Integer z) &&
                (params.get("level") instanceof String levelName)) {
            
            // 验证坐标范围
            if (x < MIN_XZ || x > MAX_XZ || z < MIN_XZ || z > MAX_XZ || y < MIN_Y || y > MAX_Y) {
                return MCPMessage.responseFailedBuilder()
                                 .id(request.getId())
                                 .error(new MCPError(MCPError.INVALID_PARAMS, "coordinates are out of valid range", null))
                                 .build();
            }
            
            var server = CUtil.getServer.get();
            Level level = switch (levelName) {
                case "overworld" -> server.getLevel(Level.OVERWORLD);
                case "nether" -> server.getLevel(Level.NETHER);
                case "end" -> server.getLevel(Level.END);
                default -> null;
            };
            
            if (level == null)
                return MCPMessage.responseFailedBuilder()
                                 .id(request.getId())
                                 .error(new MCPError(MCPError.INVALID_PARAMS, "level is not a valid dimension", null))
                                 .build();
            
            try {
                Block block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
                
                return MCPMessage.responseSuccessBuilder()
                                 .id(request.getId())
                                 .result(block.getName().getString())
                                 .build();
            } catch (Exception e) {
                return MCPMessage.responseFailedBuilder()
                                 .id(request.getId())
                                 .error(new MCPError(MCPError.INTERNAL_ERROR, "failed to get block at specified position", e.getMessage()))
                                 .build();
            }
        }
        
        return MCPMessage.responseFailedBuilder()
                         .id(request.getId())
                         .error(new MCPError(MCPError.INVALID_PARAMS, "x or y or z is not int or level is not string", null))
                         .build();
    }
}
