package dev.franzin.spyglass.ui.hudmanager;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.franzin.spyglass.ui.Clean;
import dev.franzin.spyglass.ui.Spyglass_Overlay;

public class DefaultHudManager implements IHudManager {

    @Override
    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        player.getHudManager().setCustomHud(playerRef, hud);
    }

    @Override
    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        // that's a crazy workaround to remove the HUD while the API is still broken
        player.getHudManager().setCustomHud(playerRef, new Clean(playerRef));
    }
}

