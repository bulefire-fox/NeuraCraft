package com.bulefire.neuracraft.core.mcp.command.control;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CallTool extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String method = context.getArgument("method", String.class);
        String paramJson = context.getArgument("param", String.class);
        Map<String, Object> param = new Gson().fromJson(
                paramJson,
                new TypeToken<Map<String, Object>>(){}.getType()
        );
        feedback(context.getSource(), Component.translatable("neuracraft.mcp.command.call.feedback", method));
        MCPResponse response = MCPController.getInstance().callTool(
                method,
                param,
                component -> feedback(context.getSource(), component)
        );
        if (response.getResult().isError()) {
            feedback(context.getSource(), Component.translatable(
                    "neuracraft.mcp.command.call.failed",
                    method,
                    response.getResult().getContent().toString()
            ));
        } else {
            feedback(context.getSource(), Component.translatable(
                    "neuracraft.mcp.command.call.success",
                    method,
                    response.getResult().getContent().toString()
            ));
            
        }
        
        return SINGLE_SUCCESS;
    }
}
