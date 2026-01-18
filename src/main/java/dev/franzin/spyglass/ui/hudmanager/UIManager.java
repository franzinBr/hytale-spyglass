package dev.franzin.spyglass.ui.hudmanager;


import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;


public final class UIManager {

    private static final UIManager INSTANCE = new UIManager();
    private UIManager() {
        defineHudManager();
    }
    public static UIManager getInstance() { return INSTANCE ;}

    private IHudManager hudManager;

    private void defineHudManager() {
        PluginBase multiHudPlugin = PluginManager.get().getPlugin(PluginIdentifier.fromString("Buuz135:MultipleHUD"));

        if (multiHudPlugin == null) {
            this.hudManager = new DefaultHudManager();
            return;
        }

        try {
            this.hudManager = new MultiHudManager();
        } catch (NoClassDefFoundError e) {
            this.hudManager = new DefaultHudManager();
        }

    }

    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        this.hudManager.setCustomHud(player, playerRef, id, hud);
    }
    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        this.hudManager.hideCustomHud(player, playerRef, id);
    }

}
