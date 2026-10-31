package com.loademon.afkfarm.fake;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.loademon.afkfarm.farm.Farm;
import com.loademon.afkfarm.farm.FarmStore;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.WorldLoadEvent;

/** Keeps fake players from showing up where real players would: death messages and advancements. */
public final class FakePlayerListener implements Listener {

    private final FakePlayers fakePlayers;
    private final FarmStore farms;

    public FakePlayerListener(final FakePlayers fakePlayers, final FarmStore farms) {
        this.fakePlayers = fakePlayers;
        this.farms = farms;
    }

    // /kill bypasses invulnerability; a dead fake player would broadcast a death message and never respawn.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(final PlayerDeathEvent event) {
        final Player player = event.getPlayer();
        if (this.fakePlayers.isFake(player.getUniqueId())) {
            event.setCancelled(true);
            player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdvancement(final PlayerAdvancementCriterionGrantEvent event) {
        if (this.fakePlayers.isFake(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    // Farms in worlds that load after startup (e.g. via a world manager plugin).
    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        final String worldName = event.getWorld().getName();
        for (final Farm farm : this.farms.all()) {
            if (farm.world().equals(worldName)) {
                this.fakePlayers.spawn(farm);
            }
        }
    }
}
