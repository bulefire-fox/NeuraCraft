package com.bulefire.neuracraft;

import com.bulefire.neuracraft.mod.config.InitM;
import lombok.extern.log4j.Log4j2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import com.bulefire.neuracraft.mod.command.ModCommandRegister;
import com.bulefire.neuracraft.mod.config.BaseConfig;
import com.bulefire.neuracraft.mod.event.listener.ChatEventListener;
import com.bulefire.neuracraft.mod.event.listener.ClientChatEventListener;
import com.bulefire.neuracraft.mod.event.listener.GameCloseEventListener;
import com.bulefire.neuracraft.mod.event.listener.ModLoadEventListener;
import com.bulefire.neuracraft.mod.event.listener.PlayerExitEventListener;
import com.bulefire.neuracraft.mod.event.listener.PlayerJoinEventListener;
import com.bulefire.neuracraft.mod.event.listener.ServerStartedEventListener;

import java.nio.file.Path;

@Mod(NeuraCraft.MOD_ID)
@Log4j2
public class NeuraCraft {
    public static final String MOD_ID = "neuracraft";

    public static final Path configPath = FMLPaths.CONFIGDIR.get();
    public static final Path modsPath = FMLPaths.MODSDIR.get();

    public static boolean firstLoaded = false;

    public NeuraCraft(IEventBus modEventBus, ModContainer modContainer) {
        log.info("start init NC");
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, BaseConfig.COMMON_SPEC);
        modEventBus.register(BaseConfig.class);
        modEventBus.register(ModLoadEventListener.class);
        NeoForge.EVENT_BUS.register(ModCommandRegister.class);
        NeoForge.EVENT_BUS.register(GameCloseEventListener.class);
        NeoForge.EVENT_BUS.register(PlayerExitEventListener.class);
        NeoForge.EVENT_BUS.register(PlayerJoinEventListener.class);
        NeoForge.EVENT_BUS.register(ServerStartedEventListener.class);
        NeoForge.EVENT_BUS.register(ChatEventListener.class);
        if (FMLEnvironment.getDist().isClient()) {
            NeoForge.EVENT_BUS.register(ClientChatEventListener.class);
        }
        // 初始化必须严格遵循NC->M->C->Core顺序
        // 初始化操作可以注入任意合理的部分
        InitM.init();
        firstLoaded = true;
    }

    public static void afterInit(){
        InitM.afterInit();
    }
}
