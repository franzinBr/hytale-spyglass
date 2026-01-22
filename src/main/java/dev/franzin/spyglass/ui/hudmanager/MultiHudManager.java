package dev.franzin.spyglass.ui.hudmanager;

import com.buuz135.mhud.MultipleCustomUIHud;
import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.franzin.spyglass.ui.Clean;

public class MultiHudManager implements IHudManager {
    private final MultipleHUD multipleHud;
    private final Semver multipleHudVersion;

    public MultiHudManager() {
        this.multipleHud = MultipleHUD.getInstance();
        this.multipleHudVersion = this.multipleHud.getManifest().getVersion();
    }

    @Override
    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        multipleHud.setCustomHud(player, playerRef, id, hud);
    }

    @Override
    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        if (!hasMoreHuds(player) && this.multipleHudVersion.compareTo(Semver.fromString("1.0.1")) > 0) {
            multipleHud.setCustomHud(player, playerRef, id, new Clean(playerRef));
            return;
        }

        multipleHud.hideCustomHud(player, playerRef, id);
    }

    private boolean hasMoreHuds(Player player) {
        try {
            CustomUIHud hud = player.getHudManager().getCustomHud();

            if (!(hud instanceof MultipleCustomUIHud multipleCustomUIHud)) {
                return false;
            }

            return multipleCustomUIHud.getCustomHuds().size() > 1;
        } catch (IllegalAccessError e) {
            return false;
        }
    }
}
