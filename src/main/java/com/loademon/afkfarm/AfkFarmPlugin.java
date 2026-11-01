package com.loademon.afkfarm;

import com.loademon.afkfarm.command.AfkFarmCommand;
import com.loademon.afkfarm.fake.FakePlayerListener;
import com.loademon.afkfarm.fake.FakePlayers;
import com.loademon.afkfarm.farm.Farm;
import com.loademon.afkfarm.farm.FarmStore;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.File;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public final class AfkFarmPlugin extends JavaPlugin {

    private FarmStore farms;
    private FakePlayers fakePlayers;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.farms = new FarmStore(new File(this.getDataFolder(), "farms.yml"), this.getLogger());
        this.farms.load();
        this.fakePlayers = new FakePlayers(this);

        final AfkFarmCommand command = new AfkFarmCommand(
            this.farms, this.fakePlayers, () -> this.getConfig().getInt("max-farms-per-player", 2)
        );
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register("afkfarm", "AFK farm noktalarını yönetir", List.of("afk"), command)
        );
        this.getServer().getPluginManager().registerEvents(new FakePlayerListener(this.fakePlayers, this.farms), this);

        int spawned = 0;
        for (final Farm farm : this.farms.all()) {
            if (this.fakePlayers.spawn(farm)) {
                spawned++;
            }
        }
        this.getLogger().info("Started " + spawned + "/" + this.farms.all().size() + " AFK farms");
    }

    @Override
    public void onDisable() {
        if (this.fakePlayers != null) {
            this.fakePlayers.despawnAll();
        }
    }
}
