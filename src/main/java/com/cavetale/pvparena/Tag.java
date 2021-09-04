package com.cavetale.pvparena;

import java.io.Serializable;
import java.util.ArrayList;
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
    protected int idleTime = 0;
    protected List<String> worlds = new ArrayList<>();
    protected String worldName = null;
    protected int worldUsed = 0;
    protected int totalPlayers = 0;
    protected WinRule winRule = WinRule.LAST_SURVIVOR;
    protected SpecialRule specialRule = SpecialRule.NONE;
    protected Map<UUID, Gladiator> gladiators = new HashMap<>();
    protected boolean limitedLives = false;
    protected boolean event = false;
    protected long shuffleCooldown; // epoch
    protected UUID moleUuid;
    protected boolean useSquads;
    protected List<Squad> squads;
}

