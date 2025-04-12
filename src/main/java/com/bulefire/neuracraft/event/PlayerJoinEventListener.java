package com.bulefire.neuracraft.event;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.bulefire.neuracraft.ai.control.player.PlayerMetaInfo;
import com.bulefire.neuracraft.config.yy.BaseInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = NeuraCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerJoinEventListener {
    private static final Logger logger = LoggerFactory.getLogger(PlayerJoinEventListener.class);

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.@NotNull PlayerLoggedInEvent event) throws InterruptedException {
        logger.info("Catch Player Join");
        Player player = event.getEntity();
        String name = player.getName().getString();

        PlayerControl.put(name, new PlayerMetaInfo());

        MutableComponent message = AIControl.dealWith(name, "加入聊天");
        if (!message.getString().startsWith("Error, 请联系管理员")){
            player.sendSystemMessage(Component.translatable("neuracraft.chat.message.format.player", BaseInformation.show_name, message));
            return;
        }
        player.sendSystemMessage(message);
    }
}
