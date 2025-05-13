package com.cavetale.pvparena;

import com.cavetale.mytems.Mytems;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static com.cavetale.pvparena.Items.item;
import static com.cavetale.pvparena.Items.leather;
import static com.cavetale.pvparena.Items.potion;
import static java.time.Duration.ofSeconds;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.inventory.EquipmentSlot.*;

/**
 * All kits with content and some methods to deliver them.
 */
public enum Kit {
    SWORDSMAN {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_SWORD);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(Enchantment.SHARPNESS, 4)),
                          OFF_HAND, new ItemStack(Material.SHIELD),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(Enchantment.PROTECTION, 4)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.LONG_STRENGTH));
        }
    },
    AXE_WARRIOR {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_AXE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_AXE, Map.of(Enchantment.SHARPNESS, 5)),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4, Enchantment.THORNS, 1)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(Enchantment.PROTECTION, 4, Enchantment.THORNS, 1)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.SPLASH_POTION, PotionType.STRONG_SLOWNESS));
        }
    },
    NINJA {
        @Override public ItemStack getIcon() {
            return Mytems.BLIND_EYE.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(Enchantment.SHARPNESS, 5)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.INVISIBILITY),
                           potion(Material.POTION, PotionType.STRONG_STRENGTH),
                           potion(Material.POTION, PotionType.STRONG_LEAPING));
        }
    },
    ARCHER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.BOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(Enchantment.POWER, 5, Enchantment.PUNCH, 5, Enchantment.INFINITY, 1)),
                          HEAD, item(Material.CHAINMAIL_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, item(Material.CHAINMAIL_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 5)),
                          LEGS, item(Material.CHAINMAIL_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 5)),
                          FEET, item(Material.CHAINMAIL_BOOTS, Map.of(Enchantment.PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.ARROW, 64),
                           potion(Material.POTION, PotionType.STRONG_SWIFTNESS));
        }
    },
    CROSSBOWMAN {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.CROSSBOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            ItemStack xbow = item(Material.CROSSBOW, Map.of(Enchantment.QUICK_CHARGE, 4,
                                                            Enchantment.MULTISHOT, 1,
                                                            Enchantment.PIERCING, 1));
            return Map.of(HAND, xbow.clone(),
                          OFF_HAND, xbow.clone(),
                          HEAD, item(Material.IRON_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(Enchantment.PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.TIPPED_ARROW, 16, PotionType.HARMING),
                           new ItemStack(Material.ARROW, 64));
        }
    },
    TANK {
        @Override public ItemStack getIcon() {
            return Mytems.DEFLECTOR_SHIELD.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.IRON_SWORD, Map.of()),
                          OFF_HAND, Mytems.DEFLECTOR_SHIELD.createItemStack(),
                          HEAD, item(Material.NETHERITE_HELMET, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          CHEST, item(Material.NETHERITE_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4)),
                          LEGS, item(Material.NETHERITE_LEGGINGS, Map.of(Enchantment.BLAST_PROTECTION, 4)),
                          FEET, item(Material.NETHERITE_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.STRONG_TURTLE_MASTER));
        }
    },
    POTION_MASTER {
        @Override public ItemStack getIcon() {
            final ItemStack icon = new ItemStack(Material.LINGERING_POTION);
            icon.setData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
            return icon;
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(Enchantment.FLAME, 1)),
                          HEAD, item(Material.GOLDEN_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, item(Material.GOLDEN_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.GOLDEN_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.GOLDEN_BOOTS, Map.of(Enchantment.FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getInventoryItems() {
            return List.of(potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Harming"),
                                  List.of(PotionEffectType.INSTANT_DAMAGE, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(1), ofSeconds(10)),
                                  List.of(1, 1)),
                           potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Poison"),
                                  List.of(PotionEffectType.POISON, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(10), ofSeconds(10)),
                                  List.of(1, 1)),
                           potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Blindness"),
                                  List.of(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(15), ofSeconds(10)),
                                  List.of(0, 1)));
        }

        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.SPECTRAL_ARROW, 64));
        }
    },
    TRIDENT_THROWER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.TRIDENT);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.TRIDENT, Map.of(Enchantment.LOYALTY, 3,
                                                              Enchantment.CHANNELING, 1,
                                                              Enchantment.IMPALING, 5)),
                          HEAD, item(Material.IRON_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(Enchantment.PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of();
        }
    },
    HEALER {
        @Override public ItemStack getIcon() {
            return Mytems.HEART.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            final Color c = Color.fromRGB(0xFFFFFF);
            return Map.of(HEAD, leather(Material.LEATHER_HELMET, c, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, leather(Material.LEATHER_CHESTPLATE, c, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          LEGS, leather(Material.LEATHER_LEGGINGS, c, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          FEET, leather(Material.LEATHER_BOOTS, c, Map.of(Enchantment.FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getInventoryItems() {
            return List.of(potion(Material.LINGERING_POTION, PotionType.STRONG_HEALING),
                           potion(Material.SPLASH_POTION, PotionType.LONG_REGENERATION),
                           potion(Material.LINGERING_POTION, PotionType.STRONG_STRENGTH));
        }
    },
    ROCKETEER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.FIREWORK_ROCKET);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.CROSSBOW, Map.of(Enchantment.QUICK_CHARGE, 3)),
                          HEAD, item(Material.LEATHER_HELMET, Map.of(Enchantment.BLAST_PROTECTION, 3)),
                          CHEST, new ItemStack(Material.ELYTRA),
                          LEGS, item(Material.LEATHER_LEGGINGS, Map.of(Enchantment.BLAST_PROTECTION, 3)),
                          FEET, Mytems.STOMPERS.createItemStack());
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.SLOW_FALLING),
                           rocket(32),
                           new ItemStack(Material.TNT, 32));
        }
    },
    HAMMERITE {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.MACE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.MACE, Map.of(Enchantment.DENSITY, 5,
                                                           Enchantment.BREACH, 4,
                                                           Enchantment.WIND_BURST, 3)),
                          HEAD, Mytems.EASTER_HELMET.createItemStack(),
                          CHEST, Mytems.EASTER_CHESTPLATE.createItemStack(),
                          LEGS, Mytems.EASTER_LEGGINGS.createItemStack(),
                          FEET, Mytems.EASTER_BOOTS.createItemStack());
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.WIND_CHARGE, 8));
        }
    }
    ;

    public abstract ItemStack getIcon();

    public abstract Map<EquipmentSlot, ItemStack> getEquipmentItems();

    public List<ItemStack> getInventoryItems() {
        return List.of();
    }

    public List<ItemStack> getRespawnItems() {
        return List.of();
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> result = new ArrayList<>();
        result.addAll(getEquipmentItems().values());
        result.addAll(getInventoryItems());
        result.addAll(getRespawnItems());
        return result;
    }

    public String getDisplayName() {
        return toCamelCase(" ", this);
    }

    /**
     * Call this when the player should get the kit.
     */
    public void apply(Player player) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : getEquipmentItems().entrySet()) {
            player.getEquipment().setItem(entry.getKey(), entry.getValue());
        }
        for (ItemStack item : getInventoryItems()) {
            player.getInventory().addItem(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }

    /**
     * Call this when the kit owner respawns.
     * Currently unused because we give a new kit on every death.
     * See commit `Give new kit after death`
     */
    public void onRespawn(Player player) {
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().removeItemAnySlot(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }

    private static ItemStack rocket(final int amount) {
        ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET, amount);
        rocket.editMeta(m -> {
                FireworkMeta meta = (FireworkMeta) m;
                meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL)
                               .withColor(Color.RED).build());
                meta.setPower(4);
            });
        return rocket;
    }
}
