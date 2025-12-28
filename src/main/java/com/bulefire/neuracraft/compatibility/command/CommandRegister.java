package com.bulefire.neuracraft.compatibility.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandRegister {
    @Getter
    private static final List<LiteralArgumentBuilder<CommandSourceStack>> literalArgumentBuilders = new ArrayList<>();

    public static void registerCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        literalArgumentBuilders.add(literalArgumentBuilder);
    }

    public static void registerCommands(@NotNull List<LiteralArgumentBuilder<CommandSourceStack>> literalArgumentBuilders) {
        CommandRegister.literalArgumentBuilders.addAll(literalArgumentBuilders);
    }

    //private static List<FullCommand> commands;

//    public static void registerCommands(List<FullCommand> commands) {
//        CommandRegister.commands = commands;
//    }
//
//    public static void build() {
//        for (FullCommand fullCommand : commands) {
//            // 处理注册根命令
//            // 拿到根命令的builder
//            var builder = Commands.literal(fullCommand.literal())
//                    // 根命令权限应该是正常的,判断以后再想办法整吧
//                    .requires(s -> s.hasPermission(fullCommand.permissionLevel()));
//            if (fullCommand.argument() != FullCommand.EMPTY_ARGUMENT && fullCommand.argument() != FullCommand.ARGUMENT_IS_SUBCOMMAND) {
//                // 按理来说应该是可以的
//                registerArguments(builder,fullCommand.argument().arguments());
//            }
//            // 处理子命令
//        }
//    }
//
//    private static void registerArguments(@NotNull LiteralArgumentBuilder<CommandSourceStack> builder, @NotNull List<FullCommand.CommandArgument.Argument> arguments){
//        // 对于多个参数的场景,我们使用递归
//        // 创建一个builder便于递归
//        var nowArgument = arguments.get(0);
//        var inBuilder = Commands.argument(nowArgument.description(), nowArgument.type());
//        arguments.remove(0);
//        if (!arguments.isEmpty()) {
//            registerArguments(inBuilder,arguments);
//        }
//        builder.then(inBuilder);
//    }
//
//    private static void registerArguments(@NotNull RequiredArgumentBuilder<CommandSourceStack,String> builder, @NotNull List<FullCommand.CommandArgument.Argument> arguments){
//        if (arguments.isEmpty()) {
//            return; // 递归终止条件
//        }
//        var nowArgument = arguments.get(0);
//        var inBuilder = Commands.argument(nowArgument.description(), nowArgument.type());
//        arguments.remove(0);
//        if (!arguments.isEmpty()){
//            registerArguments(inBuilder,arguments);
//        }
//        builder.then(inBuilder);
//    }
//
//    public static List<LiteralArgumentBuilder<CommandSourceStack>> getLiteralArgumentBuilders() {
//        build();
//        return literalArgumentBuilders;
//    }
}
