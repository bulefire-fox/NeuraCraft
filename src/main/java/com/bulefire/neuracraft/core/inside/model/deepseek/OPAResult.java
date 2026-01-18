package com.bulefire.neuracraft.core.inside.model.deepseek;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OPAResult {
    private String id;
    private String provider;
    private String model;
    private String object;
    private Integer created;
    private List<ChoicesBean> choices;
    private Usage usage;

    @Setter
    @Getter
    public static class ChoicesBean {
        private Object logprobs;
        private String finish_reason;
        private String native_finish_reason;
        private Integer index;
        private Message message;

        @Setter
        @Getter
        public static class Message {
            private String role;
            private String content;
            private Object refusal;
            private Object reasoning;

        }

    }

    @Setter
    @Getter
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
        private Object prompt_tokens_details;

    }

}
