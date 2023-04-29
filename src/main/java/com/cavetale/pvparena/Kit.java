package com.cavetale.pvparena;

import com.cavetale.mytems.Mytems;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static com.cavetale.pvparena.Items.*;
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
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(SHARPNESS, 5)),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(PROTECTION, 4)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(PROTECTION, 4)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(PROTECTION, 4)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.GOLDEN_APPLE),
                           potion(Material.POTION, PotionType.STRENGTH, PotionPotency.UPGRADED));
        }
    },
    AXE_WARRIOR {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_AXE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_AXE, Map.of(SHARPNESS, 5)),
                          OFF_HAND, item(Material.SHIELD, Map.of()),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(PROTECTION, 4, Enchantment.THORNS, 3)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(PROTECTION, 4, Enchantment.THORNS, 3)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(PROTECTION, 4, Enchantment.THORNS, 3)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(PROTECTION, 4, Enchantment.THORNS, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.GOLDEN_APPLE),
                           potion(Material.SPLASH_POTION, PotionType.SLOWNESS, PotionPotency.UPGRADED));
        }
    },
    NINJA {
        @Override public ItemStack getIcon() {
            return Mytems.BLIND_EYE.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            ItemStack sword = item(Material.GOLDEN_SWORD, Map.of(SHARPNESS, 3));
            sword.editMeta(meta -> {
                    for (Map.Entry<Attribute, AttributeModifier> entry : sword.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND).entries()) {
                        if (entry.getKey() == Attribute.GENERIC_ATTACK_SPEED) continue;
                        meta.addAttributeModifier(entry.getKey(), entry.getValue());
                    }
                    meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                                              new AttributeModifier(UUID.fromString("aabdaa48-116a-4051-94ce-362d1530c5f3"),
                                                                    "pvp_arena_ninja", 8.0,
                                                                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                });
            return Map.of(HAND, sword);
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.INVISIBILITY, PotionPotency.EXTENDED),
                           potion(Material.POTION, PotionType.INVISIBILITY, PotionPotency.EXTENDED),
                           potion(Material.POTION, PotionType.NIGHT_VISION, PotionPotency.EXTENDED),
                           potion(Material.POTION, PotionType.SPEED, PotionPotency.UPGRADED));
        }
    },
    ARCHER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.BOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(POWER, 5, PUNCH, 5, INFINITY, 1)),
                          HEAD, item(Material.CHAINMAIL_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.CHAINMAIL_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 5)),
                          LEGS, item(Material.CHAINMAIL_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 5)),
                          FEET, item(Material.CHAINMAIL_BOOTS, Map.of(PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.ARROW, 64),
                           potion(Material.POTION, PotionType.SPEED, PotionPotency.UPGRADED),
                           potion(Material.POTION, PotionType.SPEED, PotionPotency.UPGRADED));
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
                          HEAD, item(Material.IRON_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            ItemStack damageArrow = potion(Material.TIPPED_ARROW, PotionType.INSTANT_DAMAGE, PotionPotency.UPGRADED);
            damageArrow.setAmount(64);
            return List.of(damageArrow);
        }
    },
    TANK {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.SHIELD);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.IRON_SWORD, Map.of()),
                          OFF_HAND, Mytems.VENGEANCE_SHIELD.createItemStack(),
                          HEAD, item(Material.NETHERITE_HELMET, Map.of(PROTECTION, 5)),
                          CHEST, item(Material.NETHERITE_CHESTPLATE, Map.of(PROTECTION, 5)),
                          LEGS, item(Material.NETHERITE_LEGGINGS, Map.of(PROTECTION, 5)),
                          FEET, item(Material.NETHERITE_BOOTS, Map.of(PROTECTION, 5)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.GOLDEN_APPLE),
                           potion(Material.POTION, PotionType.TURTLE_MASTER, PotionPotency.UPGRADED),
                           potion(Material.POTION, PotionType.FIRE_RESISTANCE, PotionPotency.UPGRADED));
        }
    },
    POTION_MASTER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.LINGERING_POTION);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(FLAME, 1)),
                          HEAD, item(Material.GOLDEN_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.GOLDEN_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.GOLDEN_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.GOLDEN_BOOTS, Map.of(FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.LINGERING_POTION, PotionType.INSTANT_DAMAGE, PotionPotency.UPGRADED),
                           potion(Material.LINGERING_POTION, PotionType.INSTANT_DAMAGE, PotionPotency.UPGRADED),
                           potion(Material.LINGERING_POTION, PotionType.POISON, PotionPotency.EXTENDED),
                           potion(Material.LINGERING_POTION, PotionType.SLOWNESS, PotionPotency.EXTENDED),
                           potion(Material.LINGERING_POTION, PotionEffectType.BLINDNESS, Duration.ofSeconds(10), 0),
                           potion(Material.SPLASH_POTION, PotionType.REGEN, PotionPotency.EXTENDED),
                           potion(Material.SPLASH_POTION, PotionType.REGEN, PotionPotency.EXTENDED),
                           new ItemStack(Material.SPECTRAL_ARROW, 64));
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
                          HEAD, item(Material.IRON_HELMET, Map.of(PROTECTION, 4)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of();
        }
    },
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
     */
    public void onRespawn(Player player) {
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().removeItemAnySlot(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }
}
