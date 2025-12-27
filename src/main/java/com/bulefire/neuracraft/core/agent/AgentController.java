package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.command.CommandRegister;
import com.bulefire.neuracraft.compatibility.entity.SendMessage;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerExitEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerJoinEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import com.bulefire.neuracraft.core.Agent;
import com.bulefire.neuracraft.core.agent.commnd.NCCommand;
import com.bulefire.neuracraft.core.config.NCMainConfig;
import com.bulefire.neuracraft.core.plugin.PluginLoader;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.annotation.RegisterAgent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

@Log4j2
public class AgentController {
    @Getter
    private static final AgentManager agentManager = new AgentManager();
    @Getter
    private static final PlayerManager playerManager = new PlayerManager();
    private static final AgentGameCommand agentGameCommand = new AgentGameCommand();

    private static final List<Runnable> agentClassInitFunctions = new ArrayList<>();

    @Setter
    private static String prefix = NCMainConfig.getPrefix();

    public static void registerAgentClassInitFunction(Runnable fun){
        log.debug("register agent class init function {}", fun);
        agentClassInitFunctions.add(fun);
    }

    public static void init() {
        // 加载配置文件

        log.debug("register to chatEvent");
        // 监听聊天事件
        ChatEventProcesser.registerFun(AgentController::onMessage);
        // 监听玩家加入事件
        PlayerJoinEventProcesser.registerFun(
                (msg)->{
                    // 将玩家加入管，注意到manager不会覆盖原有值,因此与从配置文件添加的玩家不冲突
                    playerManager.addPlayer(msg.player(), null);
                    String message = NCMainConfig.getPrefix()+msg.player().toFormatedString()+"join the game";
                    // 稍加处理即可
                    onMessage(
                            new ChatEventProcesser.ChatMessage(
                                    message,
                                    msg.player(),
                                    msg.env()
                            )
                    );
                }
        );
        // 监听玩家退出事件
        PlayerExitEventProcesser.registerFun(
                (msg)->{
                    // 稍加处理即可
                    String message = NCMainConfig.getPrefix()+msg.player().toFormatedString()+"exit the game";
                    onMessage(
                            new ChatEventProcesser.ChatMessage(
                                    message,
                                    msg.player(),
                                    msg.env()
                            )
                    );
                    playerManager.removePlayer(msg.player());
                }
        );
        // 监听服务器关闭事件
        // 保存所有Agent
        ServerStoppingEventProcesser.registerFun(
                AgentController::saveAllAgentToFile
        );

        // 注册我们自己的指令
        CommandRegister.registerCommand(NCCommand.getCommands());

        // 扫描插件的注册
        // 放在InitCore里了

        // 扫描我们自己的Agent类
        var methods = AnnotationsMethodScanner.scanPackageToMethod("com.bulefire.neuracraft.core", Set.of(RegisterAgent.class));
        log.info("MMM found methods {}",methods);

        for (Method method : methods){
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to invoke method , report to the mod develop, not NC!", e);
                throw new RuntimeException(e);
            }
        }

        // 执行所有Agent类的初始化逻辑
        log.debug("all functions {}",agentClassInitFunctions);
        for (Runnable fun : agentClassInitFunctions){
            log.debug("Running agent class init function {}", fun);
            fun.run();
        }
        // 加载所有Agent,必须在Agent类加载完后加载,否则无法注入Function
        loadAllAgentFromFile();
        // 加载所有的指令
        //CommandRegister.registerCommands(agentGameCommand.getAllCommands());
        CommandRegister.registerCommands(agentGameCommand.getAllCommands());

        // 加载玩家
        playerManager.loadPlayerFromAgentManager(agentManager);
    }
    // 消息入口
    public static void onMessage(@NotNull ChatEventProcesser.ChatMessage chatMessage) {
        if (!chatMessage.msg().startsWith(prefix)) return;
        String msg = chatMessage.msg().substring(prefix.length());
        if (Objects.requireNonNull(chatMessage.env()) == ChatEventProcesser.ChatMessage.Env.CLIENT) {
            if (CUtil.hasMod.apply(NeuraCraft.MOD_ID)) {
                // 服务端有模组,交给服务端处理
                return;
            }
        }
        // 单人和服务器唯一的区别是发送消息至聊天栏的方式,但这是兼容层的事情,在这里我们把他当作同一个东西
        // 当服务器没有模组时当作单人处理
        // 保留 SINGLE 的唯一目的是在调用兼容层方法时作为环境传入,以便兼容层做出合理的处理

        // uuid 最为 agent 的唯一标识符
        UUID uuid = playerManager.getPlayerAgentUUID(chatMessage.player());
        if (uuid == null) {
            // 玩家不在聊天室则 uuid 为 null
            // 提示即可
            CUtil.sendMessageToPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.command.find.notInChatRoom"),
                            chatMessage.env(),
                            chatMessage.player())
            );
            return;
        }
        // 获取聊天室
        Agent agent = agentManager.getAgent(uuid);

        String remessage;
        // 这里异常是最好的办法,尽管这样会让代码变得臃肿一些
        // 判断 null 的话就太不优雅了
        // 虽然现在也不是很优雅 :D
        try {
            // 邪恶的作用域
            remessage = agent.sendMessage(new AgentMessage(msg, chatMessage.player()));
        } catch (AgentOutOfTime e) {
            // 提示即可
            CUtil.broadcastMessageToGroupPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.chat.error.tooFast"),
                            chatMessage.env(),
                            chatMessage.player()),
                    agent.getPlayers()
            );
            return;
        }

        // 在成功之后保存聊天室,防止崩溃导致的数据丢失
        agent.saveToFile(
                // path/to/config/agent/<modelName>/<uuid>.json
                FileUtil.agent_base_url.resolve(agent.getModelName()).resolve(agent.getUUID().toString())
        );

        // 返回格式化消息
        // 这里使用 Component 是因为 Component 为minecraft内置接口,与具体loader无关
        CUtil.broadcastMessageToGroupPlayer(
                new SendMessage(
                        Component.translatable("neuracraft.chat.message.format.player", agent.getDisPlayName(), remessage),
                        chatMessage.env(),
                        chatMessage.player()),
                agent.getPlayers()
        );
    }

//    private static void registerCommands(){
//        agentGameCommand.registerCommand(
//                new FullCommand(
//                        "chatroom",
//                        2,
//                        FullCommand.ARGUMENT_IS_SUBCOMMAND,
//                        FullCommand.EMPTY_EXECUTE_COMMAND,
//                        List.of(
//                                new FullCommand(
//                                        "create",
//                                        FullCommand.USE_FATHER_PERMISSION_LEVEL,
//                                        new FullCommand.CommandArgument()
//                                                .addArgument(StringArgumentType.string(), "roomName")
//                                                .addArgument(StringArgumentType.string(), "modelName"),
//
//                                        context -> {
//                                            String roomName = StringArgumentType.getString(context, "roomName");
//                                            String modelName = StringArgumentType.getString(context, "modelName");
//                                            Agent agent;
//                                            try {
//                                                agent = agentManager.creatAgent(modelName);
//                                            } catch (NoAgentFound e){
//                                                throw new CommandSyntaxException(
//                                                        new SimpleCommandExceptionType(Component.literal(e.toString())),
//                                                        Component.literal(e.getMessage()));
//                                            }
//                                            agent.setName(roomName);
//                                            APlayer player = new APlayer(Objects.requireNonNull(context.getSource().getPlayer()).getName().getString(), context.getSource().getPlayer().getUUID());
//                                            agent.addPlayer(player);
//                                            agent.addAdmin(player);
//                                            playerManager.addPlayer(player,agent.getUUID());
//                                            agent.saveToFile(FileUtil.agent_base_url.resolve(agent.getModelName()).resolve(agent.getUUID().toString()));
//                                            var server = CUtil.getServer.get();
//                                            ChatEventProcesser.ChatMessage.Env env = CUtil.getEnv(server);
//                                            PlayerJoinEventProcesser.onPlayerJoin(new PlayerJoinEventProcesser.JoinMessage(player, env));
//                                            return 1;
//                                        },
//                                        List.of(FullCommand.EMPTY_SUBCOMMAND),
//                                        true
//                                )
//                        ),
//                        false
//                )
//        );
//    }

    private static void loadAllAgentFromFile(){
        List<Path> paths;
        try {
            // 获取所有文件
            paths = FileUtil.readAllFilePath(FileUtil.agent_base_url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Path path : paths) {
            if(path.toFile().isFile()){
                // 让Agent自己判断文件是否是自己的
                String agentName = agentManager.getAgentByConfigFilePath(path);
                if (agentName == null){
                    // 找不到模型
                    throw new RuntimeException("can not find agentName model for " + path);
                }
                // 创建聊天室并加载
                agentManager.creatAgent(agentName).loadFromFile(path);
            }
        }
    }
    private static void saveAllAgentToFile(){
        for (Agent agent : agentManager.getAllAgents()) {
            agent.saveToFile(
                    // path/to/config/agent/<modelName>/<uuid>
                    FileUtil.agent_base_url.resolve(agent.getModelName()).resolve(agent.getUUID().toString())
            );
        }
    }
}
