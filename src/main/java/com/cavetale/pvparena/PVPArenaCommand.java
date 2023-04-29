package com.cavetale.pvparena;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import java.util.List;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class PVPArenaCommand extends AbstractCommand<PVPArenaPlugin> {
    protected PVPArenaCommand(final PVPArenaPlugin plugin) {
        super(plugin, "pvparena");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("vote").arguments("[path]")
            .completers(CommandArgCompleter.supplyList(() -> List.copyOf(plugin.pvpArenaMaps.maps.keySet())))
            .description("Vote on a map")
            .hidden(true)
            .playerCaller(this::vote);
    }

    private boolean vote(Player player, String[] args) {
        if (args.length == 0) {
            if (!plugin.pvpArenaMaps.isVoteActive()) throw new CommandWarn("The vote is over");
            plugin.pvpArenaMaps.openVoteBook(player);
            return true;
        } else if (args.length == 1) {
            if (!plugin.pvpArenaMaps.isVoteActive()) throw new CommandWarn("The vote is over");
            PVPArenaMap pvpArenaMap = plugin.pvpArenaMaps.maps.get(args[0]);
            if (pvpArenaMap == null) throw new CommandWarn("Map not found!");
            plugin.pvpArenaMaps.vote(player.getUniqueId(), pvpArenaMap);
            player.sendMessage(text("You voted for " + pvpArenaMap.getDisplayName(), GREEN));
            return true;
        } else {
            return false;
        }
    }
}
