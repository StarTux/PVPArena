package com.cavetale.pvparena;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class Items {
    public static final Enchantment FLAME = Enchantment.ARROW_FIRE;
    public static final Enchantment PUNCH = Enchantment.ARROW_KNOCKBACK;
    public static final Enchantment UNBREAKING = Enchantment.DURABILITY;
    public static final Enchantment SHARPNESS = Enchantment.DAMAGE_ALL;
    public static final Enchantment INFINITY = Enchantment.ARROW_INFINITE;
    public static final Enchantment POWER = Enchantment.ARROW_DAMAGE;
    public static final Enchantment PROTECTION = Enchantment.PROTECTION_ENVIRONMENTAL;
    public static final Enchantment PROJECTILE_PROTECTION = Enchantment.PROTECTION_PROJECTILE;
    public static final Enchantment FIRE_PROTECTION = Enchantment.PROTECTION_FIRE;
    public static final Enchantment BLAST_PROTECTION = Enchantment.PROTECTION_EXPLOSIONS;
    private Items() { }

    public static ItemStack potion(Material material, PotionType potionType, PotionPotency potency) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(m -> {
                PotionMeta meta = (PotionMeta) m;
                boolean extended = potency == PotionPotency.EXTENDED && potionType.isExtendable();
                boolean upgraded = potency == PotionPotency.UPGRADED && potionType.isUpgradeable();
                meta.setBasePotionData(new PotionData(potionType, extended, upgraded));
            });
        return itemStack;
    }

    public static ItemStack potion(Material material, PotionEffectType type, Duration duration, int amplifier) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(m -> {
                PotionMeta meta = (PotionMeta) m;
                final int ticks = (int) (duration.toMillis() / 50L);
                PotionEffect effect = new PotionEffect(type, ticks, amplifier, true, false, true);
                meta.addCustomEffect(effect, true);
                meta.setColor(type.getColor());
                meta.displayName(text(toCamelCase(" ", List.of(type.getName().split("_"))), WHITE));
            });
        return itemStack;
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
