package com.bulefire.neuracraft.core.inside.mcp.mcquery;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.core.mcp.AbsMCPTool;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class WeatherQuery extends AbsMCPTool {
    
    @RegisterMCP
    public static void init() {
        log.info("WeatherQuery init");
        MCPController.getInstance().getMcpManager().registerTool(new WeatherQuery());
    }
    
    public WeatherQuery() {
        super(
                "weather_query",
                "查询当前游戏的天气,返回 [晴天,下雨,雷雨] 并同时返回 [-1,1] 间的雨量",
                MCPToolInfo.builder()
                        .type(MCPToolInfo.Type.LOCAL)
                           .host(URI.create(MCPToolInfo.Type.LOCAL.getHead()+"weather_query"))
                           .method("tool.game.query.weather")
                           .params(Map.of("level", new MCPToolInfo.Param("string","查询的维度, 在[overworld,nether,end]之中", String.class)))
                           .build()
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request) {
        var params = request.getParams();
        if (!params.containsKey("level"))
            return MCPMessage.responseFailedBuilder()
                             .id(request.getId())
                             .error(new MCPError(MCPError.INVALID_REQUEST, "level is null", null))
                             .build();
        if (params.get("level") instanceof String level) {
            var server = CUtil.getServer.get();
            return switch (level) {
                case "overworld" -> MCPMessage.responseSuccessBuilder()
                                              .id(request.getId())
                                              .result("overworld 当前天气: "+ getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.OVERWORLD))))
                                              .build();
                case "nether" -> MCPMessage.responseSuccessBuilder()
                                           .id(request.getId())
                                           .result("nether 当前天气: "+getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.NETHER))))
                                           .build();
                case "end" -> MCPMessage.responseSuccessBuilder()
                                 .id(request.getId())
                                 .result("end 当前天气: "+getWeatherAndRainfall(Objects.requireNonNull(server.getLevel(Level.END))))
                                 .build();
                default -> MCPMessage.responseFailedBuilder()
                                 .id(request.getId())
                                 .error(new MCPError(MCPError.INVALID_PARAMS, "level is not a valid dimension", null))
                                 .build();
            };
        }
        return MCPMessage.responseFailedBuilder()
                        .id(request.getId())
                .error(new MCPError(MCPError.INVALID_PARAMS, "level is not a string", null))
                        .build();
    }
    
    private @NotNull String getWeatherAndRainfall(@NotNull ServerLevel level) {
        if (level.isThundering()) return "雷雨 雨量: " + level.getRainLevel(1.0f);
        if (level.isRaining()) return "下雨 雨量: " + level.getRainLevel(1.0f);
        return "晴天 雨量: 0.0";
    }
}
