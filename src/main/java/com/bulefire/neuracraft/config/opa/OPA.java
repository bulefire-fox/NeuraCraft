package com.bulefire.neuracraft.config.opa;

import com.bulefire.neuracraft.config.Config;
import com.bulefire.neuracraft.config.yy.Variables;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

public class OPA {
    private static final Logger log = LogUtils.getLogger();

    public static final ForgeConfigSpec.Builder BUILDER;

    // API地址
    public static final ForgeConfigSpec.ConfigValue<String> API_URL;
    // API 接口
    public static final ForgeConfigSpec.ConfigValue<String> API_INTERFACE;
    // Token
    public static final ForgeConfigSpec.ConfigValue<String> TOKEN;
    // variables
    public static final Variables VARIABLES;
    // model
    public static final ForgeConfigSpec.ConfigValue<String> MODEL;
    // systemPrompt
    public static final ForgeConfigSpec.ConfigValue<String> SYSTEM_PROMPT;
    // show name
    public static final ForgeConfigSpec.ConfigValue<String> SHOW_NAME;
    // save chat
    public static final ForgeConfigSpec.ConfigValue<Boolean> SAVE_CHAT;
    // times / min
    public static final ForgeConfigSpec.ConfigValue<Integer> TIMES;

    static{
        BUILDER = Config.BUILDER;

        BUILDER.push("OpenAI API AI 设置");

        API_URL = BUILDER.comment("API URL").define("apiUrl", "https://openrouter.ai/api");
        API_INTERFACE = BUILDER.comment("API 接口").define("apiInterface", "/v1/chat/completions");
        TOKEN = BUILDER.comment("Token").define("token", "sk-or-v1-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        VARIABLES = new Variables();
        MODEL = BUILDER.comment("Model").define("model", "deepseek/deepseek-chat-v3-0324:free");
        SYSTEM_PROMPT = BUILDER.comment("System Prompt").define("systemPrompt", "你是deepseek");
        SHOW_NAME = BUILDER.comment("显示名称").define("showName", "deepseek");
        SAVE_CHAT = BUILDER.comment("保存聊天").define("saveChat", true);
        TIMES = BUILDER.comment("次数 / min").define("times", 20);

        BUILDER.pop();
    }

    public OPA(){

    }

    public static String api_url;
    public static String api_interface;
    public static String token;
    public static String model;
    public static String system_prompt;
    public static String show_name;
    public static boolean save_chat;
    public static int times;

    public void init(){
        // log.info("初始化银影AI设置");
        api_url = API_URL.get();
        api_interface = API_INTERFACE.get();
        token = TOKEN.get();
        VARIABLES.init();
        model = MODEL.get();
        system_prompt = SYSTEM_PROMPT.get();
        show_name = SHOW_NAME.get();
        save_chat = SAVE_CHAT.get();
        times = TIMES.get();
    }
}
