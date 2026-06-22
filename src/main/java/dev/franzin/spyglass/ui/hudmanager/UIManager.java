package dev.franzin.spyglass.ui.hudmanager;


import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;


public final class UIManager {

    private static final UIManager INSTANCE = new UIManager();
    private UIManager() {}
    public static UIManager getInstance() { return INSTANCE ;}

    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        if (player.getHudManager().getCustomHud(id) != null) {
            player.getHudManager().removeCustomHud(playerRef, id);
        }
        player.getHudManager().addCustomHud(playerRef, hud);
    }

    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        player.getHudManager().removeCustomHud(playerRef, id);
    }

}
