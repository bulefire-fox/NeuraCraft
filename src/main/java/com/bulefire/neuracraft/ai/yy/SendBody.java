package com.bulefire.neuracraft.ai.yy;

public class SendBody {
    private String appId;
    private String chatId;
    private Variables variables;
    private String model;
    private String systemPrompt;
    private String message;

    public static class Variables {
        private String nickName;
        private String furryCharacter;
        private String promptPatch;

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getFurryCharacter() {
            return furryCharacter;
        }

        public void setFurryCharacter(String furryCharacter) {
            this.furryCharacter = furryCharacter;
        }

        public String getPromptPatch() {
            return promptPatch;
        }

        public void setPromptPatch(String promptPatch) {
            this.promptPatch = promptPatch;
        }
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Variables getVariables() {
        if (variables == null) {
            variables = new Variables();
        }
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
