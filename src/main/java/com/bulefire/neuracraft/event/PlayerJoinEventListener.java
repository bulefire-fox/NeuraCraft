package com.bulefire.neuracraft.event;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.ai.yy.YY;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = NeuraCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerJoinEventListener {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.@NotNull PlayerLoggedInEvent event){
        Player player = event.getEntity();
        String message = YY.dealWith(player.getName().getString(), "加入聊天");
        player.sendSystemMessage(Component.translatable(message));
    }
}
