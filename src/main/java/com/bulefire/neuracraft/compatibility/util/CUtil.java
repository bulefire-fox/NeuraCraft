package com.bulefire.neuracraft.compatibility.util;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.SendMessage;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.core.util.InitFailedException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.units.qual.A;
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
    public static Function<String,Boolean> hasMod;
    public static Supplier<MinecraftServer> getServer;

    public static Supplier<Player> getPlayer = () -> {throw (InitFailedException)(new InitFailedException("CUtil.getPlayer not inject").initCause(new NullPointerException("CUtil.getPlayer is null")));};
    @Contract(pure = true)
    public static void broadcastMessageToCharBar(@NotNull SendMessage message){
        switch (message.env()){
            case CLIENT,SERVER -> {
                MinecraftServer server = getServer.get();
                for(ServerPlayer player : server.getPlayerList().getPlayers()){
                    player.sendSystemMessage(message.message());
                }
            }
            case SINGLE -> Objects.requireNonNullElse(Minecraft.getInstance().player,getPlayer.get()).sendSystemMessage(message.message());
        }
    }
    @Contract(pure = true)
    public static void broadcastMessageToGroupPlayer(@NotNull SendMessage message, @NotNull List<APlayer> playerGroup){
        switch (message.env()){
            case CLIENT, SERVER -> {
                MinecraftServer server = getServer.get();
                for(APlayer player : playerGroup){
                    Objects.requireNonNull(server.getPlayerList().getPlayer(player.uuid())).sendSystemMessage(message.message());
                }
            }
            case SINGLE -> Objects.requireNonNullElse(Minecraft.getInstance().player,getPlayer.get()).sendSystemMessage(message.message());
        }
    }
    @Contract(pure = true)
    public static void sendMessageToPlayer(@NotNull SendMessage message){
        switch (message.env()){
            case CLIENT, SERVER -> {
                MinecraftServer server = getServer.get();
                Objects.requireNonNull(server.getPlayerList().getPlayer(message.player().uuid())).sendSystemMessage(message.message());
            }
            case SINGLE -> Objects.requireNonNullElse(Minecraft.getInstance().player,getPlayer.get()).sendSystemMessage(message.message());
        }
    }

    public static @NotNull String AiPOST(@NotNull String urls, @NotNull String body, @NotNull String token) throws Exception {
        log.info("send to ai url: {}", urls);
        log.info("send to ai: {}", body);
        log.info("send to ai token: {}", token);

        var connection = getConnection(urls, body, token);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            log.info("Response from AI: {}", response);

            return response.toString();
        } else {
            return "POST request failed with response code: " + responseCode+","+connection.getResponseMessage();
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
