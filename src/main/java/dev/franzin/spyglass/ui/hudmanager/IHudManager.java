package dev.franzin.spyglass.ui.hudmanager;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public interface IHudManager {
    void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud);
    void hideCustomHud(Player player, PlayerRef playerRef, String id);
}

