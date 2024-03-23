package com.cavetale.pvparena;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Json serialized player data storage per round.
 */
@Getter @RequiredArgsConstructor
public final class Gladiator implements Serializable {
    protected final UUID uuid;
    protected final String name;
    protected int lives;
    protected int score;
    protected int kills;
    protected int deaths;
    protected boolean gameOver = false;
    protected double health;
    protected boolean dead = false;
    protected long respawnCooldown; // epoch
    protected int respawnCooldownDisplay;
    protected long invulnerable; // epoch
    protected int squad;
    protected Kit kit;
    protected UUID lastDamagedBy;
    protected int money;

    public Gladiator(final Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.health = player.getHealth();
    }

    public boolean is(Player player) {
        return player.getUniqueId().equals(uuid);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
