package com.cavetale.pvparena;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Tag implements Serializable {
    protected ArenaState state = ArenaState.IDLE;
    protected int gameTime = 0;
    protected boolean suddenDeath = false;
    protected boolean warmUp = false;
    protected int endTime = 0;
    protected String worldName = null;
    protected int totalPlayers = 0;
    protected WinRule winRule = WinRule.LAST_SURVIVOR;
    protected SpecialRule specialRule = SpecialRule.NONE;
    protected Map<UUID, Gladiator> gladiators = new HashMap<>();
    protected boolean limitedLives = false;
    protected boolean event = false;
    protected boolean pause = false;
    protected long shuffleCooldown; // epoch
    protected UUID moleUuid;
    protected boolean useSquads;
    protected List<Squad> squads;
    protected Map<UUID, Integer> scores = new HashMap<>();
    protected boolean debug;

    protected int getScore(UUID uuid) {
        return scores.getOrDefault(uuid, 0);
    }

    public void addScore(UUID uuid, int score) {
        scores.put(uuid, Math.max(0, getScore(uuid) + score));
    }
}
