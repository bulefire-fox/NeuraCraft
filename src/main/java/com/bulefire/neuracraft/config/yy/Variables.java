package com.bulefire.neuracraft.config.yy;

import com.bulefire.neuracraft.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

public class Variables {
    private static final Logger log = LogUtils.getLogger();

    public static final ForgeConfigSpec.Builder BUILDER;

    // nickname
    public static final ForgeConfigSpec.ConfigValue<String> NICKNAME;
    // furry character
    public static final ForgeConfigSpec.ConfigValue<String> FURRY_CHARACTER;
    // prompt patch
    public static final ForgeConfigSpec.ConfigValue<String> PROMPT_PATCH;

    static {
        BUILDER = BaseInformation.BUILDER;
        BUILDER.push("Variables");

        NICKNAME = BUILDER.comment("昵称").define("nickname","没有哦");
        FURRY_CHARACTER = BUILDER.comment("设定").define("furryCharacter","没有哦");
        PROMPT_PATCH = BUILDER.comment("微调提示词").define("promptPatch","你在和一群人对话,每个人以 [name] 前缀区分");

        BUILDER.pop();
    }

    public Variables(){

    }

    public static String nickname;
    public static String furry_charter;
    public static String prompt_patch;

    public void init(){
        nickname = NICKNAME.get();
        furry_charter = FURRY_CHARACTER.get();
        prompt_patch = PROMPT_PATCH.get();
    }

}
