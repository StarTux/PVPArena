package com.cavetale.pvparena;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.item.ItemKinds;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.mytems.util.Items.tooltip;
import static com.cavetale.pvparena.PVPArenaPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class KitMenu {
    private final Player player;
    private final Gladiator gladiator;

    public void open() {
        final int size = 6 * 9;
        GuiOverlay.Builder builder = GuiOverlay.BLANK.builder(size, color(0xD04040))
            .title(text("Kit Menu", color(0x202020)));
        Gui gui = new Gui(plugin());
        int nextIndex = 0;
        for (Kit kit : Kit.values()) {
            if (plugin().tag.winRule == WinRule.LAST_SURVIVOR) {
                if (kit == Kit.ROCKETEER) continue;
                if (kit == Kit.NINJA) continue;
                if (kit == Kit.HEALER) continue;
            }
            ItemStack icon = kit.getIcon();
            icon.editMeta(meta -> {
                    tooltip(meta, List.of(text(kit.getDisplayName(), color(0xD04040)),
                                          text(""),
                                          textOfChildren(Mytems.MOUSE_LEFT, text(" View kit items", GRAY))));
                    meta.displayName(text(kit.getDisplayName(), color(0xD04040)));
                    meta.addItemFlags(ItemFlag.values());
                });
            int index = nextIndex++;
            gui.setItem(index % 3 + 3, index / 3 + 1, icon, click -> {
                    if (!click.isLeftClick()) return;
                    open(kit);
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                });
        }
        gui.size(size).title(builder.build());
        gui.open(player);
    }

    public void open(Kit kit) {
        final int size = 6 * 9;
        GuiOverlay.Builder builder = GuiOverlay.BLANK.builder(size, color(0xD0D040))
            .title(text(kit.getDisplayName(), color(0x202020)));
        Gui gui = new Gui(plugin());
        int nextIndex = 0;
        for (ItemStack item : kit.getAllItems()) {
            gui.setItem(nextIndex++, item);
        }
        ItemStack confirmItem = kit.getIcon();
        confirmItem.editMeta(meta -> {
                tooltip(meta, List.of(textOfChildren(Mytems.MOUSE_LEFT, text(" Confirm", GREEN, BOLD))));
                meta.addItemFlags(ItemFlag.values());
            });
        gui.setItem(5 * 9 + 4, confirmItem, click -> {
                if (!click.isLeftClick()) return;
                player.closeInventory();
                if (!KitItem.isKitItem(player.getInventory().getItemInMainHand())) return;
                gladiator.kit = kit;
                player.getInventory().clear();
                kit.apply(player);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                if (plugin().tag.useSquads) {
                    Squad squad = plugin().getSquad(player);
                    if (squad != null) {
                        for (Player other : Bukkit.getOnlinePlayers()) {
                            if (plugin().getSquad(other) == squad) {
                                other.sendMessage(textOfChildren(plugin().PVP,
                                                                 text(" " + player.getName() + " picked "),
                                                                 ItemKinds.icon(kit.getIcon()),
                                                                 text(" " + kit.getDisplayName(), GOLD)));
                            }
                        }
                    }
                }
            });
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                open();
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
            });
        gui.size(size).title(builder.build());
        gui.open(player);
    }
}
