/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public class Spyglass_Overlay extends CustomUIHud {

    private static final String DEFAULT_UI_PATH = "Spyglass/Spyglass_Overlay.ui";
    private final String overlayTexturePath;

    public Spyglass_Overlay(@NotNull PlayerRef playerRef, String overlayTexturePath) {
        super(playerRef);
        this.overlayTexturePath = overlayTexturePath;
    }

    @Override
    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
        if (overlayTexturePath == null || overlayTexturePath.isBlank()) {
            uiCommandBuilder.append(DEFAULT_UI_PATH);
            return;
        }

        String texturePath = overlayTexturePath
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String inlineUi =
                "@MyTex = PatchStyle(TexturePath: \"" + texturePath + "\");\n" +
                "Group {\n" +
                "  Background: @MyTex;\n" +
                "  LayoutMode: Left;\n" +
                "  Anchor: (Top: 0, Left: 0);\n" +
                "}";

        uiCommandBuilder.appendInline(null, inlineUi);
    }
}
