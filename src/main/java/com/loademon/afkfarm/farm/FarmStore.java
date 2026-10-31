package com.loademon.afkfarm.farm;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

/** Keeps farms in farms.yml. Names are case-insensitive and unique across the server. */
public final class FarmStore {

    private final File file;
    private final Logger logger;
    private final Map<String, Farm> farms = new LinkedHashMap<>();

    public FarmStore(final File file, final Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public void load() {
        this.farms.clear();
        if (!this.file.exists()) {
            return;
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
        final ConfigurationSection section = yaml.getConfigurationSection("farms");
        if (section == null) {
            return;
        }

        for (final String name : section.getKeys(false)) {
            final ConfigurationSection farm = section.getConfigurationSection(name);
            if (farm == null) {
                continue;
            }
            try {
                this.farms.put(key(name), new Farm(
                    name,
                    UUID.fromString(farm.getString("owner", "")),
                    farm.getString("owner-name", "?"),
                    farm.getString("world", "world"),
                    farm.getDouble("x"),
                    farm.getDouble("y"),
                    farm.getDouble("z"),
                    (float) farm.getDouble("yaw"),
                    (float) farm.getDouble("pitch")
                ));
            } catch (final IllegalArgumentException e) {
                this.logger.warning("Skipping farm '" + name + "' in farms.yml: invalid owner UUID");
            }
        }
    }

    public void save() {
        final YamlConfiguration yaml = new YamlConfiguration();
        for (final Farm farm : this.farms.values()) {
            final String path = "farms." + farm.name() + ".";
            yaml.set(path + "owner", farm.owner().toString());
            yaml.set(path + "owner-name", farm.ownerName());
            yaml.set(path + "world", farm.world());
            yaml.set(path + "x", farm.x());
            yaml.set(path + "y", farm.y());
            yaml.set(path + "z", farm.z());
            yaml.set(path + "yaw", farm.yaw());
            yaml.set(path + "pitch", farm.pitch());
        }
        try {
            yaml.save(this.file);
        } catch (final IOException e) {
            this.logger.log(Level.SEVERE, "Could not save farms.yml", e);
        }
    }

    public @Nullable Farm get(final String name) {
        return this.farms.get(key(name));
    }

    public Collection<Farm> all() {
        return Collections.unmodifiableCollection(this.farms.values());
    }

    public long countOwnedBy(final UUID owner) {
        return this.farms.values().stream().filter(farm -> farm.owner().equals(owner)).count();
    }

    public void add(final Farm farm) {
        this.farms.put(key(farm.name()), farm);
        this.save();
    }

    public void remove(final String name) {
        this.farms.remove(key(name));
        this.save();
    }

    private static String key(final String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
