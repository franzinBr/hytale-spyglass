package dev.franzin.spyglass.ui.hudmanager;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class MultiHudManager implements IHudManager {
    private final MultipleHUD multipleHud;

    public MultiHudManager() {
        this.multipleHud = MultipleHUD.getInstance();
    }

    @Override
    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        multipleHud.setCustomHud(player, playerRef, id, hud);
    }

    @Override
    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        multipleHud.hideCustomHud(player, playerRef, id);
    }
}
