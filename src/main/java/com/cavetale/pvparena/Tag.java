package com.cavetale.pvparena;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Tag implements Serializable {
    ArenaState state = ArenaState.IDLE;
    int gameTime = 0;
    boolean suddenDeath = false;
    boolean warmUp = false;
    int endTime = 0;
    int idleTime = 0;
    List<String> worlds = new ArrayList<>();
    String worldName = null;
    int worldUsed = 0;
    int totalPlayers = 0;
    WinRule winRule = WinRule.LAST_SURVIVOR;
    SpecialRule specialRule = SpecialRule.NONE;
    Map<UUID, Gladiator> gladiators = new HashMap<>();
    boolean limitedLives = false;
    boolean event = false;
    long shuffleCooldown; // epoch
    UUID moleUuid;
}

