package com.cavetale.pvparena;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class SpectatorCommand implements CommandExecutor {
    private final PVPArenaPlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (plugin.toggleSpectatorMode(player)) {
            player.sendMessage(text("Marked as spectator", GREEN));
        } else {
            player.sendMessage(text("Marked as player", GREEN));
        }
        return true;
    }
}
