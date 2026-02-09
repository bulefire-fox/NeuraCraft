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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@MCP
@Log4j2
public class TimeQuery extends AbsMCPTool {
    @RegisterMCP
    public static void init() {
        log.info("TimeQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new TimeQuery());
    }
    
    public TimeQuery() {
        super(
                "time_query",
                "查询当前游戏的时间,返回HH:MM:SS格式的游戏时间,SS时间不精确",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .name("tool.game.query.time")
                           .params(Map.of("level",new MCPToolInfo.Param("string","查询的维度, 在[overworld,nether,end]之中", String.class)))
                           .build()
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
        var params = request.getParams();
        if (!params.containsKey("level"))
            return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_REQUEST, "level is null", null)))
                             .build();
        if (params.get("level") instanceof String level) {
            var server = CUtil.getServer.get();
            return switch (level) {
                case "overworld" -> MCPMessage.responseBuilder()
                                              .id(request.getId())
                                              .result(MCPResponse.Result.of("overworld 当前时间: "+ getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)))))
                                              .build();
                case "nether" -> MCPMessage.responseBuilder()
                                           .id(request.getId())
                                           .result(MCPResponse.Result.of("nether 当前时间: "+getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.NETHER)))))
                                           .build();
                case "end" -> MCPMessage.responseBuilder()
                                        .id(request.getId())
                                        .result(MCPResponse.Result.of("end 当前时间: "+getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.END)))))
                                        .build();
                default -> MCPMessage.responseBuilder()
                                     .id(request.getId())
                                     .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "level is not a valid dimension", null)))
                                     .build();
            };
        }
        return MCPMessage.responseBuilder()
                         .id(request.getId())
                         .result(MCPResponse.Result.of(new MCPError(MCPError.INVALID_PARAMS, "level is not a string", null)))
                         .build();
    }
    
    private @NotNull String getWeatherAndRainfall(@NotNull ServerLevel level) {
        long gameTime = level.getDayTime();
        // 计算一天中的时间（取模24000）
        long timeOfDay = gameTime % 24000;
        
        // 转换为小时、分钟、秒
        int hours = (int)(timeOfDay / 1000);  // 每小时1000ticks
        int minutes = (int)((timeOfDay % 1000) * 60 / 1000);  // 每分钟约16.67ticks
        
        // 计算秒数（可选，基于更精确的计算）
        int seconds = (int)(((timeOfDay % 1000) * 3600) / 1000 % 60);
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
