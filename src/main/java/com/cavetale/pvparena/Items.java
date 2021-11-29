package com.cavetale.pvparena;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class Items {
    private Items() { }

    public static ItemStack potionItem(ItemStack itemStack, PotionType potionType) {
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        meta.setBasePotionData(new PotionData(potionType));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack potionItem(Material material, PotionType potionType) {
        return potionItem(new ItemStack(material), potionType);
    }

    public static ItemStack potionItem(ItemStack itemStack, PotionEffectType type, int duration) {
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        PotionEffect effect = new PotionEffect(type, duration, 0, true, false, true);
        meta.addCustomEffect(effect, true);
        meta.setColor(type.getColor());
        meta.displayName(Component.text(type.getName()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack potionItem(Material material, PotionEffectType type, int duration) {
        return potionItem(new ItemStack(material), type, duration);
    }

    public static ItemStack loadedCrossbow(ItemStack loaded) {
        ItemStack itemStack = new ItemStack(Material.CROSSBOW);
        CrossbowMeta meta = (CrossbowMeta) itemStack.getItemMeta();
        meta.addChargedProjectile(loaded);
        meta.displayName(loaded.getItemMeta().displayName());
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
