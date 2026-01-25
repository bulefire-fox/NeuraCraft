package com.bulefire.neuracraft.compatibility.util;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.SendMessage;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.core.util.InitFailedException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
public class CUtil {
    public static Function<String, Boolean> hasMod;
    public static Supplier<MinecraftServer> getServer;

    public static Supplier<Player> getPlayer = () -> {
        throw (InitFailedException) (new InitFailedException("CUtil.getPlayer not inject").initCause(new NullPointerException("CUtil.getPlayer is null")));
    };

    @Contract(pure = true)
    public static void broadcastMessageToCharBar(@NotNull SendMessage message) {
        switch (message.env()) {
            case CLIENT, SERVER -> {
                MinecraftServer server = getServer.get();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.sendSystemMessage(message.message());
                }
            }
            case SINGLE -> Objects.requireNonNull(getPlayer.get()).sendSystemMessage(message.message());
        }
    }

    @Contract(pure = true)
    public static void broadcastMessageToGroupPlayer(@NotNull SendMessage message, @NotNull List<APlayer> playerGroup) {
        switch (message.env()) {
            case CLIENT, SERVER -> {
                MinecraftServer server = getServer.get();
                for (APlayer player : playerGroup) {
                    Objects.requireNonNull(server.getPlayerList().getPlayer(player.uuid())).sendSystemMessage(message.message());
                }
            }
            case SINGLE -> Objects.requireNonNull(getPlayer.get()).sendSystemMessage(message.message());
        }
    }

    @Contract(pure = true)
    public static void sendMessageToPlayer(@NotNull SendMessage message) {
        switch (message.env()) {
            case CLIENT, SERVER -> {
                MinecraftServer server = getServer.get();
                Objects.requireNonNull(server.getPlayerList().getPlayer(message.player().uuid())).sendSystemMessage(message.message());
            }
            case SINGLE -> Objects.requireNonNull(getPlayer.get()).sendSystemMessage(message.message());
        }
    }

    /**
     * 发送AI类的 POST 请求
     *
     * @param urls  请求地址
     * @param body  请求体
     * @param token 密钥, 不要 {@code Bearer} 前缀
     * @return 响应
     * @throws Exception 异常
     * @author bulefire_fox
     * @apiNote 默认请求头为:
     *         <pre>
     *                             {@code
     *                                  Content-Type: application/json
     *                                  Authorization: Bearer <token>
     *                             }
     *                         </pre>
     * @since 1.0
     */
    public static @NotNull Response AiPOST(@NotNull String urls, @NotNull String body, @NotNull String token) throws IOException {
        var connection = getConnection(urls, body, token);

        log.info("POST request sent to: {}", urls);
        log.info("token: {}", token);
        log.info("body: {}", body);

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        var result = new Response(response.toString(), connection.getResponseMessage(), responseCode);
        log.info("Response: {}", result);
        return result;
    }

    public record Response(String response, String responseMessage, int status) {
        @Override
        public @NotNull String toString() {
            return "Response{" +
                    "response='" + response + '\'' +
                    ", responseMessage='" + responseMessage + '\'' +
                    ", status=" + status +
                    '}';
        }

        public @NotNull String getFormatted() {
            return "POST request failed with response code: %s, %s msg:/ %s".formatted(status, responseMessage, response);
        }
    }

    private static @NotNull HttpURLConnection getConnection(@NotNull String urls, @NotNull String body, @NotNull String token) throws IOException {
        URL url = new URL(urls);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);

        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    public static ChatEventProcesser.ChatMessage.@NotNull Env getEnv(MinecraftServer server) {
        ChatEventProcesser.ChatMessage.Env env;
        if (server == null) {
            env = ChatEventProcesser.ChatMessage.Env.CLIENT;
        } else if (server.isDedicatedServer()) {
            env = ChatEventProcesser.ChatMessage.Env.SERVER;
        } else {
            env = ChatEventProcesser.ChatMessage.Env.SINGLE;
        }
        return env;
    }
}
