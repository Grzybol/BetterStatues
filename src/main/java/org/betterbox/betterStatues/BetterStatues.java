package org.betterbox.betterStatues;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class BetterStatues extends JavaPlugin {
    private CommandManager commandManager;
    String folderPath;
    PluginLogger pluginLogger;
    FileManager fileManager;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        // Plugin startup logic


        folderPath =getDataFolder().getAbsolutePath();
        try{
            Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            pluginLogger = new PluginLogger(getDataFolder().getAbsolutePath(), defaultLogLevels,this);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: onEnable: calling ConfigManager");
        }catch (Exception e){
            getServer().getLogger().warning("PluginLogger Exception: " + e.getMessage());
        }
        configManager = new ConfigManager(this, pluginLogger, folderPath);
        fileManager = new FileManager(getDataFolder().getAbsolutePath(),this,this);
        getCommand("bs").setExecutor(new CommandManager(this,this,fileManager));


    }

    public String getUserUUID(String username) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        Pattern pattern = Pattern.compile("\"id\":\"(\\w+)\"");
        Matcher matcher = pattern.matcher(response.toString());
        if (matcher.find()) {
            return matcher.group(1);  // Zwraca znaleziony UUID
        } else {
            return null;  // Brak UUID w odpowiedzi, prawdopodobnie nieprawidłowa nazwa użytkownika
        }
    }
    public String getPlayerSkinURL(String uuid) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        Pattern pattern = Pattern.compile("\"url\":\"(http[^\"]+)\"");
        Matcher matcher = pattern.matcher(response.toString());
        if (matcher.find()) {
            return matcher.group(1);  // Zwraca URL skóry
        } else {
            return null;  // Brak URL w odpowiedzi
        }
    }
    public boolean createPlayerModel(Player player, String statueName, String playerName){
        try {

            String uuid = getUserUUID(playerName);
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found.");
                return false;
            }
            String skinURL = getPlayerSkinURL(uuid);
            if (skinURL == null) {
                player.sendMessage(ChatColor.RED + "Skin not found.");
                return false;
            }

            // Tworzenie statuetki w miejscu gracza
            Location loc = player.getLocation();
            World world = player.getWorld();
            ArmorStand as = world.spawn(loc, ArmorStand.class);
            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setVisible(true);
            as.setCustomName(ChatColor.GREEN + playerName + "'s Statue");
            as.setCustomNameVisible(true);

            // Ustawienie skóry na głowie statuetki
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(playerName);  // Użyj UUID jeśli to możliwe
            head.setItemMeta(meta);
            as.setHelmet(head);

            fileManager.saveStatue(statueName,as.getUniqueId(),loc);

            player.sendMessage(ChatColor.GREEN + "Statue of " + playerName + " created successfully.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Error accessing Mojang's API.");
            return false;
        }
        return true;
    }
    public boolean deleteStatue(Player player, String statueName) {
        // Najpierw sprawdzamy, czy statuetka istnieje w danych
        FileManager.StatueData statueData = fileManager.loadStatue(statueName);
        if (statueData == null) {
            player.sendMessage(ChatColor.RED + "Statue with the name '" + statueName + "' does not exist.");
            return false;
        }

        // Usuwamy statuetkę z gry
        World world = getServer().getWorld(statueData.getLocation().getWorld().getName());
        UUID statueUUID = statueData.getUuid();

        // Pobieranie wszystkich armor standów w świecie i usunięcie odpowiedniego
        for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
            if (as.getUniqueId().equals(statueUUID)) {
                as.remove();
                break;
            }
        }

        // Usuwamy dane statuetki z pliku konfiguracyjnego
        fileManager.deleteStatue(statueName);
        player.sendMessage(ChatColor.GREEN + "Statue named '" + statueName + "' has been successfully deleted.");
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
