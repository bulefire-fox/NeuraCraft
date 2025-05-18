package com.bulefire.neuracraft.register;

import com.bulefire.neuracraft.object.tab.logo.AILogoTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.bulefire.neuracraft.NeuraCraft.MODID;

public class RegisterCreativeModeTab {
    // 创建一个延迟注册表来保存所有将在"neuracraft"命名空间下注册的创造模式标签
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final ResourceLocation DEFAULT_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/creative_inventory/tab_items.png");

    public static final RegistryObject<CreativeModeTab> AILOGO_TAB = CREATIVE_MODE_TABS.register("ai_logo", AILogoTab::new);
}
