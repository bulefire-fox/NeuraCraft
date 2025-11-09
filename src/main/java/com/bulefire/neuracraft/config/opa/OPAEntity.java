package com.bulefire.neuracraft.config.opa;

import lombok.Data;

@Data
public class OPAEntity {
    private String api_url;
    private String api_interface;
    private String token;
    private String model;
    private String system_prompt;
    private String show_name;
    private boolean save_chat;
    private int times;
    private boolean enable_multi_module;

    public OPAEntity() {
        this.api_url = OPA.api_url;
        this.api_interface = OPA.api_interface;
        this.token = OPA.token;
        this.model = OPA.model;
        this.system_prompt = OPA.system_prompt;
        this.show_name = OPA.show_name;
        this.save_chat = OPA.save_chat;
        this.times = OPA.times;
        this.enable_multi_module = OPA.enable_multi_module;
    }
}
