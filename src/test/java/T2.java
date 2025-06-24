import com.bulefire.neuracraft.util.AIHTTPClient;

public class T2 {
    public static void main(String[] args) throws Exception {
        String msg = AIHTTPClient.POST("https://openrouter.ai/api/v1/chat/completions",
                "{" +
                        "  \"model\": \"deepseek/deepseek-chat-v3-0324:free\",\n" +
                        "  \"messages\": [\n" +
                        "    {\n" +
                        "      \"role\": \"user\",\n" +
                        "      \"content\": \"1+1等于几\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"role\": \"assistant\",\n" +
                        "      \"content\": \"1 + 1 等于 **2**。\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"role\": \"user\",\n" +
                        "      \"content\": \"2+2等于几\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}",
                "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");
        System.out.println( msg);
    }
}
