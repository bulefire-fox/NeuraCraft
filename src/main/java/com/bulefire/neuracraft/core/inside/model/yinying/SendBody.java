package com.bulefire.neuracraft.core.inside.model.yinying;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SendBody {
    private String appId;
    private String chatId;
    private Variables variables;
    private String model;
    private String systemPrompt;
    private String message;

    public SendBody(String appId, String chatId, Variables variables, String model, String systemPrompt, String message) {
        this.appId = appId;
        this.chatId = chatId;
        this.variables = variables;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.message = message;
    }

    @Data
    @Accessors(chain = true)
    public static class Variables {
        private String nickName;
        private String furryCharacter;
        private String promptPatch;

        public Variables(String nickName, String furryCharacter, String promptPatch) {
            this.nickName = nickName;
            this.furryCharacter = furryCharacter;
            this.promptPatch = promptPatch;
        }
    }
}
