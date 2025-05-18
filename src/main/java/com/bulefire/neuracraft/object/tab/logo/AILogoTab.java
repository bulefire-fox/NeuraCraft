package com.bulefire.neuracraft.object.tab.logo;

import com.bulefire.neuracraft.register.RegisterCreativeModeTab;
import com.bulefire.neuracraft.register.RegisterItem;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class AILogoTab extends CreativeModeTab{
    private static final Logger log = LogUtils.getLogger();

    //private static final ResourceLocation AI_LOGO_ICON = new ResourceLocation(NeuraCraft.MODID, "textures/icons/ai_logo_icon.png");


    public AILogoTab() {
        super(get());
    }

    public static @NotNull Builder get() {
        Builder b = CreativeModeTab.builder()
                .title(Component.translatable("tab.neuracraft.ai_logo"))
                .icon(() -> RegisterItem.CYBER_FURRY_LOGO.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(RegisterItem.CYBER_FURRY_LOGO.get().getDefaultInstance());
                }).withBackgroundLocation(RegisterCreativeModeTab.DEFAULT_LOCATION);
        return b;
    }
}
