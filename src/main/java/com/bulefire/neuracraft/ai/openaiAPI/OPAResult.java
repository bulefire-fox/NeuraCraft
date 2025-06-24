package com.bulefire.neuracraft.ai.openaiAPI;

import java.util.List;

public class OPAResult {
    private String id;
    private String provider;
    private String model;
    private String object;
    private Integer created;
    private List<ChoicesBean> choices;
    private Usage usage;

    public static class ChoicesBean {
        private Object logprobs;
        private String finish_reason;
        private String native_finish_reason;
        private Integer index;
        private Message message;

        public static class Message {
            private String role;
            private String content;
            private Object refusal;
            private Object reasoning;

            public String getRole() {
                return role;
            }

            public void setRole(String role) {
                this.role = role;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public Object getRefusal() {
                return refusal;
            }

            public void setRefusal(Object refusal) {
                this.refusal = refusal;
            }

            public Object getReasoning() {
                return reasoning;
            }

            public void setReasoning(Object reasoning) {
                this.reasoning = reasoning;
            }
        }

        public Object getLogprobs() {
            return logprobs;
        }

        public void setLogprobs(Object logprobs) {
            this.logprobs = logprobs;
        }

        public String getFinish_reason() {
            return finish_reason;
        }

        public void setFinish_reason(String finish_reason) {
            this.finish_reason = finish_reason;
        }

        public String getNative_finish_reason() {
            return native_finish_reason;
        }

        public void setNative_finish_reason(String native_finish_reason) {
            this.native_finish_reason = native_finish_reason;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
        private Object prompt_tokens_details;

        public Integer getPrompt_tokens() {
            return prompt_tokens;
        }

        public void setPrompt_tokens(Integer prompt_tokens) {
            this.prompt_tokens = prompt_tokens;
        }

        public Integer getCompletion_tokens() {
            return completion_tokens;
        }

        public void setCompletion_tokens(Integer completion_tokens) {
            this.completion_tokens = completion_tokens;
        }

        public Integer getTotal_tokens() {
            return total_tokens;
        }

        public void setTotal_tokens(Integer total_tokens) {
            this.total_tokens = total_tokens;
        }

        public Object getPrompt_tokens_details() {
            return prompt_tokens_details;
        }

        public void setPrompt_tokens_details(Object prompt_tokens_details) {
            this.prompt_tokens_details = prompt_tokens_details;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public List<ChoicesBean> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoicesBean> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }
}
