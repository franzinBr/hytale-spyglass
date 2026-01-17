/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public class Clean extends CustomUIHud {

    public Clean(@NotNull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Spyglass/Clean.ui");
    }
}
