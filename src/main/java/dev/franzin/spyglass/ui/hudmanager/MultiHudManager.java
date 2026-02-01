package dev.franzin.spyglass.ui.hudmanager;

import com.buuz135.mhud.MultipleCustomUIHud;
import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.franzin.spyglass.ui.Clean;
import java.lang.reflect.Method;
import java.util.Collection;

public class MultiHudManager implements IHudManager {
    private final MultipleHUD multipleHud;
    private final boolean hasHideHudBug;

    public MultiHudManager() {
        this.multipleHud = MultipleHUD.getInstance();
        this.hasHideHudBug = multipleHud.getManifest()
                .getVersion()
                .satisfies(SemverRange.fromString("1.0.1 - 1.0.3"));
    }

    @Override
    public void setCustomHud(Player player, PlayerRef playerRef, String id, CustomUIHud hud) {
        multipleHud.setCustomHud(player, playerRef, id, hud);
    }

    @Override
    public void hideCustomHud(Player player, PlayerRef playerRef, String id) {
        // MultipleHUD 1.0.1-1.0.3 has a bug where hideCustomHud() fails
        // when only one HUD is registered. Workaround: replace with empty HUD instead.
        if (hasHideHudBug && !hasMultipleHuds(player)) {
            multipleHud.setCustomHud(player, playerRef, id, new Clean(playerRef));
            return;
        }

        multipleHud.hideCustomHud(player, playerRef, id);
    }

    private boolean hasMultipleHuds(Player player) {
        try {
            CustomUIHud hud = player.getHudManager().getCustomHud();
            if (!(hud instanceof MultipleCustomUIHud multipleCustomUIHud)) {
                return false;
            }

            Method method = MultipleCustomUIHud.class.getMethod("getCustomHuds");
            Object result = method.invoke(multipleCustomUIHud);

            return result instanceof Collection<?> c && c.size() > 1;

        } catch (Exception e) {
            return false;
        }
    }
}