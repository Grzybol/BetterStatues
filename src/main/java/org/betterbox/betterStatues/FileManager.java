package org.betterbox.betterStatues;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileManager {
    private final JavaPlugin plugin;
    private final BetterStatues betterStatues;
    private File dataFile;
    private FileConfiguration dataConfig;

    public FileManager(String folderPath, JavaPlugin plugin, BetterStatues betterStatues) {
        this.plugin = plugin;
        this.betterStatues = betterStatues;
        File logFolder = new File(folderPath, "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        dataFile = new File(logFolder, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    public void saveStatue(String statueName, UUID uuid, Location location) {
        String path = statueName + ".";
        dataConfig.set(path + "UUID", uuid.toString());
        dataConfig.set(path + "Location.world", location.getWorld().getName());
        dataConfig.set(path + "Location.x", location.getX());
        dataConfig.set(path + "Location.y", location.getY());
        dataConfig.set(path + "Location.z", location.getZ());

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public StatueData loadStatue(String statueName) {
        String path = statueName + ".";
        UUID uuid = UUID.fromString(dataConfig.getString(path + "UUID"));
        String worldName = dataConfig.getString(path + "Location.world");
        double x = dataConfig.getDouble(path + "Location.x");
        double y = dataConfig.getDouble(path + "Location.y");
        double z = dataConfig.getDouble(path + "Location.z");
        Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z);

        return new StatueData(uuid, location);
    }
    public void deleteStatue(String statueName) {
        // Usunięcie całej sekcji dotyczącej danej statuetki
        dataConfig.set(statueName, null);

        // Zapis zmian do pliku
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class StatueData {
        private UUID uuid;
        private Location location;

        public StatueData(UUID uuid, Location location) {
            this.uuid = uuid;
            this.location = location;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Location getLocation() {
            return location;
        }
    }



}


