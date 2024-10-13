package org.betterbox.betterStatues;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final BetterStatues betterStatues;
    public CommandManager(JavaPlugin plugin, BetterStatues betterStatues){
        this.plugin = plugin;
        this.betterStatues = betterStatues;

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 0:
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterStatues]" + ChatColor.AQUA + "Plugin version "+plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterStatues]" + ChatColor.AQUA + "Plugin created by "+plugin.getDescription().getAuthors());
                break;
            case 2:
                if (sender instanceof Player && (sender.isOp() || sender.hasPermission("betterstatues.create"))){
                   betterStatues.createPlayerModel((Player) sender,args[1]);

                }
        }
        return true;
    }

}
