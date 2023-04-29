package com.cavetale.pvparena;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.bukkit.persistence.PersistentDataType.STRING;

/**
 * Abstraction for the item which opens the kit menu.
 */
public final class KitItem {
    public static NamespacedKey getIdKey() {
        return new NamespacedKey(PVPArenaPlugin.instance, "id");
    }

    public static String getIdValue() {
        return "kit";
    }

    public static ItemStack spawnKitItem() {
        ItemStack item = Mytems.BOSS_CHEST.createIcon();
        item.editMeta(meta -> {
                Items.text(meta, List.of(text("Choose a Kit", GREEN),
                                         text("There are many PvP kits", GRAY),
                                         text("to choose from.", GRAY),
                                         text(""),
                                         textOfChildren(Mytems.MOUSE_LEFT, text(" Open the Kit Menu", GRAY))));
                meta.getPersistentDataContainer().set(getIdKey(), STRING, getIdValue());
            });
        return item;
    }

    public static boolean isKitItem(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.getPersistentDataContainer().has(getIdKey(), STRING)) return false;
        return getIdValue().equals(meta.getPersistentDataContainer().get(getIdKey(), STRING));
    }

    private KitItem() { }
}
