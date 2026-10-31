package com.loademon.afkfarm.fake;

import com.loademon.afkfarm.farm.Farm;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;

/**
 * Spawns one server-side player per farm. The player is added to the world but never to the player
 * list, so the game treats it as present (chunk loading, mob spawning, random ticks, despawn range)
 * while the tab list, online count and joins/quits never see it.
 */
public final class FakePlayers {

    private static final Method SET_GAME_MODE_SILENTLY;

    static {
        try {
            SET_GAME_MODE_SILENTLY = ServerPlayerGameMode.class
                .getDeclaredMethod("setGameModeForPlayer", GameType.class, GameType.class);
            SET_GAME_MODE_SILENTLY.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Unsupported server version", e);
        }
    }

    private final Logger logger;
    private final Map<String, ServerPlayer> byFarm = new HashMap<>();
    private final Map<UUID, String> farmByPlayerId = new HashMap<>();

    public FakePlayers(final Logger logger) {
        this.logger = logger;
    }

    public boolean spawn(final Farm farm) {
        final String key = key(farm.name());
        if (this.byFarm.containsKey(key)) {
            return true;
        }

        final Location location = farm.location();
        if (location == null) {
            this.logger.warning("Farm '" + farm.name() + "' is in world '" + farm.world() + "', which is not loaded");
            return false;
        }

        final MinecraftServer server = MinecraftServer.getServer();
        final ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        location.getChunk().load();

        final GameProfile profile = new GameProfile(farm.fakePlayerId(), farm.fakePlayerName());
        final ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        // Paper's chunk loader ignores players that are not marked real.
        player.isRealPlayer = true;
        new ServerGamePacketListenerImpl(server, new FakeConnection(), player, CommonListenerCookie.createInitial(profile, false));

        player.snapTo(new Vec3(farm.x(), farm.y(), farm.z()), farm.yaw(), farm.pitch());
        // Creative players still count for spawning and despawn range, but mobs ignore them
        // and phantoms skip them. Set directly so no game mode change is broadcast to clients.
        try {
            SET_GAME_MODE_SILENTLY.invoke(player.gameMode, GameType.CREATIVE, null);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set fake player game mode", e);
        }
        final CraftPlayer bukkitPlayer = player.getBukkitEntity();
        bukkitPlayer.setInvulnerable(true);
        bukkitPlayer.setInvisible(true);
        bukkitPlayer.setVisibleByDefault(false);
        bukkitPlayer.setSleepingIgnored(true);

        level.addNewPlayer(player);

        this.byFarm.put(key, player);
        this.farmByPlayerId.put(player.getUUID(), key);
        return true;
    }

    public void despawn(final String farmName) {
        final ServerPlayer player = this.byFarm.remove(key(farmName));
        if (player == null) {
            return;
        }
        this.farmByPlayerId.remove(player.getUUID());
        player.level().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    public void despawnAll() {
        for (final String farmName : this.byFarm.keySet().toArray(String[]::new)) {
            this.despawn(farmName);
        }
    }

    public boolean isSpawned(final String farmName) {
        return this.byFarm.containsKey(key(farmName));
    }

    public boolean isFake(final UUID playerId) {
        return this.farmByPlayerId.containsKey(playerId);
    }

    private static String key(final String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
