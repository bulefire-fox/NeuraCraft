package com.bulefire.neuracraft.core.inside.model.yinying;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class ResultBody {
    private String id;
    private String model;
    private Usage usage;
    private Variables variables;
    private List<ChoicesBean> choices;

    @Accessors(chain = true)
    @Data
    public static class Usage {
        private Integer total_tokens;
    }

    @Accessors(chain = true)
    @Data
    public static class Variables {
        private String nickName;
        private String furryCharacter;
        private String promptPatch;
        private String timeNow;
    }

    @Accessors(chain = true)
    @Data
    public static class ChoicesBean {
        private Message message;
        private String finish_reason;
        private Integer index;

        @Accessors(chain = true)
        @Data
        public static class Message {
            private String role;
            private String content;
        }
    }
}
