package com.cavetale.pvparena;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.fam.trophy.Highscore;
import com.cavetale.mytems.item.trophy.TrophyCategory;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class PVPAdminCommand extends AbstractCommand<PVPArenaPlugin> {
    protected PVPAdminCommand(final PVPArenaPlugin plugin) {
        super(plugin, "pvpadmin");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("start").denyTabCompletion()
            .description("Start a game")
            .senderCaller(this::start);
        rootNode.addChild("stop").denyTabCompletion()
            .description("Stop the game")
            .senderCaller(this::stop);
        rootNode.addChild("save").denyTabCompletion()
            .description("Save to disk")
            .senderCaller(this::save);
        rootNode.addChild("load").denyTabCompletion()
            .description("Load from disk")
            .senderCaller(this::load);
        rootNode.addChild("rule").arguments("<rule>")
            .completers(CommandArgCompleter.enumLowerList(SpecialRule.class))
            .description("Set the current rule")
            .senderCaller(this::rule);
        rootNode.addChild("nextworld").arguments("<world>...")
            .completers(CommandArgCompleter.supplyList(() -> plugin.getWorldList()),
                        CommandArgCompleter.REPEAT)
            .description("Set the next world(s)")
            .senderCaller(this::nextWorld);
        rootNode.addChild("clearworlds").denyTabCompletion()
            .description("Clear next worlds")
            .senderCaller(this::clearWorlds);
        rootNode.addChild("skip").denyTabCompletion()
            .description("Skip this world for the next game")
            .senderCaller(this::skip);
        rootNode.addChild("areas").denyTabCompletion()
            .description("Print areas info")
            .senderCaller(this::areas);
        rootNode.addChild("event").arguments("<value>")
            .completers(CommandArgCompleter.BOOLEAN)
            .description("Set event mode")
            .senderCaller(this::event);
        rootNode.addChild("debug").arguments("<value>")
            .completers(CommandArgCompleter.BOOLEAN)
            .description("Set debug mode")
            .senderCaller(this::debug);
        // Score
        CommandNode scoreNode = rootNode.addChild("score")
            .description("Score subcommands");
        scoreNode.addChild("reset").denyTabCompletion()
            .description("Reset scores")
            .senderCaller(this::scoreReset);
        scoreNode.addChild("add").arguments("<player> <amount>")
            .completers(CommandArgCompleter.PLAYER_CACHE,
                        CommandArgCompleter.integer(i -> i != 0))
            .description("Add scores")
            .senderCaller(this::scoreAdd);
        scoreNode.addChild("reward").denyTabCompletion()
            .description("Give score rewards")
            .senderCaller(this::scoreReward);
    }

    private void start(CommandSender sender) {
        plugin.cleanUpGame();
        plugin.startGame();
        sender.sendMessage(text("Game started!", YELLOW));
    }

    private void stop(CommandSender sender) {
        plugin.cleanUpGame();
        plugin.setIdle();
        sender.sendMessage(text("Game stopped", YELLOW));
    }

    private void save(CommandSender sender) {
        plugin.saveTag();
        sender.sendMessage(text("Tag saved", YELLOW));
    }

    private void load(CommandSender sender) {
        plugin.loadTag();
        sender.sendMessage(text("Tag loaded", YELLOW));
    }

    private boolean rule(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        plugin.tag.specialRule = SpecialRule.valueOf(args[0].toUpperCase());
        plugin.saveTag();
        sender.sendMessage(text("SpecialRule = " + plugin.tag.specialRule, AQUA));
        return true;
    }

    private boolean nextWorld(CommandSender sender, String[] args) {
        if (args.length > 0) {
            plugin.tag.worlds = new ArrayList<>(List.of(args));
            plugin.tag.worldUsed = 999;
            plugin.saveTag();
        }
        sender.sendMessage(text("Worlds coming up: " + plugin.tag.worlds, YELLOW));
        return true;
    }

    private void clearWorlds(CommandSender sender) {
        plugin.tag.worlds = new ArrayList<>();
        plugin.saveTag();
        sender.sendMessage(text("Next worlds cleared", YELLOW));
    }

    private void skip(CommandSender sender) {
        plugin.tag.worldUsed = 999;
        sender.sendMessage(text("New world next round!", YELLOW));
    }

    private void areas(CommandSender sender) {
        sender.sendMessage(text("AreasFile: " + Json.serialize(plugin.areasFile), AQUA));
    }

    private boolean event(CommandSender sender, String[] args) {
        if (args.length > 1) return false;
        if (args.length >= 1) {
            plugin.tag.event = CommandArgCompleter.requireBoolean(args[0]);
            plugin.saveTag();
        }
        sender.sendMessage(text("Event Mode: " + plugin.tag.event, YELLOW));
        return true;
    }

    private boolean debug(CommandSender sender, String[] args) {
        if (args.length > 1) return false;
        if (args.length >= 1) {
            plugin.tag.debug = CommandArgCompleter.requireBoolean(args[0]);
            plugin.saveTag();
        }
        sender.sendMessage(text("Debug Mode: " + plugin.tag.debug, YELLOW));
        return true;
    }

    private void scoreReset(CommandSender sender) {
        plugin.tag.scores.clear();
        plugin.computeHighscore();
        sender.sendMessage(text("Scores reset", AQUA));
    }

    private boolean scoreAdd(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache target = CommandArgCompleter.requirePlayerCache(args[0]);
        int value = CommandArgCompleter.requireInt(args[1], i -> i != 0);
        plugin.tag.addScore(target.uuid, value);
        plugin.saveTag();
        plugin.computeHighscore();
        sender.sendMessage(text("Score of " + target.name + " adjusted by " + value, AQUA));
        return true;
    }

    private void scoreReward(CommandSender sender) {
        int res = Highscore.reward(plugin.tag.scores,
                                   "pvp_arena",
                                   TrophyCategory.SWORD,
                                   PVPArenaPlugin.TITLE,
                                   hi -> "You earned " + hi.score + " kill" + (hi.score == 1 ? "" : "s"));
        sender.sendMessage(text(res + " players rewarded", AQUA));
    }
}
