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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class BetterStatues extends JavaPlugin {
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        getCommand("bs").setExecutor(new CommandManager(this,this));


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
    public boolean createPlayerModel(Player player, String playerName){
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

            player.sendMessage(ChatColor.GREEN + "Statue of " + playerName + " created successfully.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Error accessing Mojang's API.");
            return false;
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
