package com.bulefire.neuracraft.core.agent.commnd;

import com.bulefire.neuracraft.core.agent.commnd.admin.ListAgents;
import com.bulefire.neuracraft.core.agent.commnd.control.Create;
import com.bulefire.neuracraft.core.agent.commnd.control.Delete;
import com.bulefire.neuracraft.core.agent.commnd.self.Exit;
import com.bulefire.neuracraft.core.agent.commnd.self.Find;
import com.bulefire.neuracraft.core.agent.commnd.self.Join;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class NCCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommands(){
        return Commands.literal("agent")
                .requires(source -> source.hasPermission(2))
                .executes(new ChatRoomCommand())
                .then(Commands.literal("create")
                        .then(Commands.argument("roomName", StringArgumentType.word())
                                .then(Commands.argument("chatModel", StringArgumentType.word())
                                        .executes(new Create())
                                )
                        )
                )

                .then(Commands.literal("delete")
                        .executes(new Delete())
                        .then(Commands.argument("roomName", StringArgumentType.word())
                                .executes(new Delete())
                        )
                        .then(Commands.argument("roomUUID", StringArgumentType.string())
                                .executes(new Delete())
                        )
                )

                .then(Commands.literal("join")
                        .then(Commands.argument("roomName", StringArgumentType.word())
                                .executes(new Join())
                        )
                        .then(Commands.argument("roomUUID", StringArgumentType.string())
                                .executes(new Join())
                        )
                )

                .then(Commands.literal("exit")
                        .executes(new Exit())
                )

                .then(Commands.literal("show")
                        .executes(new Show())
                )

                .then(Commands.literal("find")
                        .executes(new Find())
                )

//                .then(Commands.literal("invite")
//                        .then(Commands.argument("inviteName", StringArgumentType.word())
//                                .executes(new Invite())
//                        )
//                )
//
//                .then(Commands.literal("kick")
//                        .then(Commands.argument("kickName", StringArgumentType.word())
//                                .executes(new Kick())
//                        )
//                )
//
                .then(Commands.literal("list")
                        .executes(new ListAgents())
                );
    }
}
