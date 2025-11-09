package com.bulefire.neuracraft.config.yy;

import lombok.Data;

@Data
public class BaseInformationEntity {
    private String api_url;
    private String api_interface;
    private String token;
    private String appid;
    private Variables variables;
    private String model;
    private String system_prompt;
    private String show_name;
    private boolean save_chat;
    private int times;
    private boolean enable_multi_module;

    public BaseInformationEntity() {
        this.api_url = BaseInformation.api_url;
        this.api_interface = BaseInformation.api_interface;
        this.token = BaseInformation.token;
        this.appid = BaseInformation.appid;
        this.variables = new Variables();
        this.model = BaseInformation.model;
        this.system_prompt = BaseInformation.system_prompt;
        this.show_name = BaseInformation.show_name;
        this.save_chat = BaseInformation.save_chat;
        this.times = BaseInformation.times;
    }


    @Data
    public static class Variables {
        private String nickname;
        private String furry_charter;
        private String prompt_patch;

        public Variables() {
            this.nickname = BaseInformation.Variables.nickname;
            this.furry_charter = BaseInformation.Variables.furry_charter;
            this.prompt_patch = BaseInformation.Variables.prompt_patch;
        }
    }
}
