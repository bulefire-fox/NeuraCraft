package com.bulefire.neuracraft.register;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.command.chatroom.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = NeuraCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommand {
    @SubscribeEvent
    public static void registerCommands(@NotNull RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("chatroom")
                        .requires(source -> source.hasPermission(2))
                        .executes(new ChatRoomCommand())
                        .then(Commands.literal("create")
                                .then(Commands.argument("roomName", StringArgumentType.word())
                                        .executes(new Create()) // 带参数的子命令
                                )
                        )

                        .then(Commands.literal("delete")
                                .then(Commands.argument("roomName", StringArgumentType.word())
                                        .executes(new Delete())
                                )
                        )

                        .then(Commands.literal("join")
                                .then(Commands.argument("roomName", StringArgumentType.word())
                                        .executes(new Join())
                                )
                        )

                        .then(Commands.literal("exit")
                                .then(Commands.argument("roomName", StringArgumentType.word())
                                        .executes(new Exit())
                                )
                        )

                        .then(Commands.literal("show")
                                .executes(new Show())
                        )

                        .then(Commands.literal("find")
                                .executes(new Find())
                        )
        );
    }
}
