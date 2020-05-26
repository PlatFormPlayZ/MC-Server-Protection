package net.TheElm.project.mixins.Commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.TheElm.project.enums.Permissions;
import net.TheElm.project.utilities.RankUtils;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.PardonCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(PardonCommand.class)
public class Pardon {
    
    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("pardon")
            .requires((source) -> source.getMinecraftServer().getPlayerManager().getIpBanList().isEnabled()
                && (source.hasPermissionLevel(3) || RankUtils.hasPermission(source, Permissions.VANILLA_COMMAND_UNBAN)))
            .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile())
                .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(context.getSource().getMinecraftServer().getPlayerManager().getUserBanList().getNames(), suggestionsBuilder))
                .executes((context) -> Pardon.pardon(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))
            )
        );
    }
    
    @Shadow
    private static int pardon(ServerCommandSource serverCommandSource, Collection<GameProfile> collection) {
        return Command.SINGLE_SUCCESS;
    }
    
}
