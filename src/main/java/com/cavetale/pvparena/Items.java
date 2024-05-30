package com.cavetale.pvparena;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class Items {
    private Items() { }

    public static ItemStack potion(Material material, int amount, PotionType potionType) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(m -> {
                PotionMeta meta = (PotionMeta) m;
                meta.setBasePotionType(potionType);
            });
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static ItemStack potion(Material material, PotionType potionType) {
        return potion(material, 1, potionType);
    }

    public static ItemStack potion(Material material, int amount, PotionEffectType type, Duration duration, int amplifier) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(m -> {
                PotionMeta meta = (PotionMeta) m;
                final int ticks = (int) (duration.toMillis() / 50L);
                PotionEffect effect = new PotionEffect(type, ticks, amplifier, true, false, true);
                meta.addCustomEffect(effect, true);
                meta.setColor(type.getColor());
                meta.displayName(text(toCamelCase(" ", List.of(type.getKey().getKey().split("_"))), WHITE));
            });
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static ItemStack potion(Material material, int amount, Component displayName, List<PotionEffectType> types, List<Duration> durations, List<Integer> amplifiers) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(m -> {
                PotionMeta meta = (PotionMeta) m;
                for (int i = 0; i < types.size(); i += 1) {
                    final PotionEffectType type = types.get(i);
                    final Duration duration = durations.get(i);
                    final int ticks = (int) (duration.toMillis() / 50L);
                    if (i == 0) meta.setColor(type.getColor());
                    final int amplifier = amplifiers.get(i);
                    PotionEffect effect = new PotionEffect(type, ticks, amplifier, true, false, true);
                    meta.addCustomEffect(effect, true);
                }
                meta.displayName(displayName);
            });
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static ItemStack potion(Material material, PotionEffectType type, Duration duration, int amplifier) {
        return potion(material, 1, type, duration, amplifier);
    }

    public static ItemStack loadedCrossbow(ItemStack loaded) {
        ItemStack itemStack = new ItemStack(Material.CROSSBOW);
        CrossbowMeta meta = (CrossbowMeta) itemStack.getItemMeta();
        meta.addChargedProjectile(loaded);
        meta.displayName(loaded.getItemMeta().displayName());
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack item(Material material, Map<Enchantment, Integer> enchantments) {
        ItemStack itemStack = new ItemStack(material);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        return itemStack;
    }

    public static ItemStack leather(Material material, Color color, Map<Enchantment, Integer> enchantments) {
        ItemStack itemStack = item(material, enchantments);
        itemStack.editMeta(m -> ((LeatherArmorMeta) m).setColor(color));
        return itemStack;
    }
}
