package com.bulefire.neuracraft.register;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.command.chatroom.*;
import com.bulefire.neuracraft.command.chatroom.admin.ListChatRooms;
import com.bulefire.neuracraft.command.chatroom.control.Create;
import com.bulefire.neuracraft.command.chatroom.control.Delete;
import com.bulefire.neuracraft.command.chatroom.player.Invite;
import com.bulefire.neuracraft.command.chatroom.player.Kick;
import com.bulefire.neuracraft.command.chatroom.self.Exit;
import com.bulefire.neuracraft.command.chatroom.self.Find;
import com.bulefire.neuracraft.command.chatroom.self.Join;
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
                                        .executes(new Create())
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

                        .then(Commands.literal("invite")
                                .then(Commands.argument("inviteName", StringArgumentType.word())
                                        .executes(new Invite())
                                )
                        )

                        .then(Commands.literal("kick")
                                .then(Commands.argument("kickName", StringArgumentType.word())
                                        .executes(new Kick())
                                )
                        )

                        .then(Commands.literal("list")
                                .executes(new ListChatRooms())
                        )
        );
    }
}
