package com.loademon.afkfarm.farm;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

/** A saved farm spot: where the fake player stands and who it belongs to. */
public record Farm(
    String name,
    UUID owner,
    String ownerName,
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {

    public static Farm at(final String name, final UUID owner, final String ownerName, final Location location) {
        return new Farm(
            name, owner, ownerName, location.getWorld().getName(),
            location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
        );
    }

    /** Returns null while the farm's world is not loaded. */
    public @Nullable Location location() {
        final World bukkitWorld = Bukkit.getWorld(this.world);
        return bukkitWorld == null ? null : new Location(bukkitWorld, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    /**
     * Stable per farm, so a restart reuses the same identity. Name-based (v3) UUIDs never collide with
     * real Mojang accounts, which are random (v4).
     */
    public UUID fakePlayerId() {
        return UUID.nameUUIDFromBytes(("AFKFarm:" + this.name).getBytes(StandardCharsets.UTF_8));
    }

    /** Profile names are capped at 16 characters. */
    public String fakePlayerName() {
        final String full = "afk_" + this.name;
        return full.length() <= 16 ? full : full.substring(0, 16);
    }
}
