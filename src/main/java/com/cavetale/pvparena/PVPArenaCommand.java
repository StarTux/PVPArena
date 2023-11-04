package com.cavetale.pvparena;

import com.cavetale.core.command.AbstractCommand;

public final class PVPArenaCommand extends AbstractCommand<PVPArenaPlugin> {
    protected PVPArenaCommand(final PVPArenaPlugin plugin) {
        super(plugin, "pvparena");
    }

    @Override
    protected void onEnable() {
    }
}
