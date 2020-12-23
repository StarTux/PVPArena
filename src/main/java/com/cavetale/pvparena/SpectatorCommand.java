package com.cavetale.pvparena;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SpectatorCommand implements CommandExecutor {
    private final PVPArenaPlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (plugin.toggleSpectatorMode(player)) {
            player.sendMessage(ChatColor.GREEN + "Marked as spectator");
        } else {
            player.sendMessage(ChatColor.GREEN + "Marked as player");
        }
        return true;
    }
}
