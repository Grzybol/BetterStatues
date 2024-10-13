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
    private final FileManager fileManager;
    public CommandManager(JavaPlugin plugin, BetterStatues betterStatues, FileManager fileManager){
        this.plugin = plugin;
        this.betterStatues = betterStatues;
        this.fileManager = fileManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 0:
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterStatues]" + ChatColor.AQUA + "Plugin version "+plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterStatues]" + ChatColor.AQUA + "Plugin created by "+plugin.getDescription().getAuthors());
                break;
            case 1:
                if(args[1].equals("help")){
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterStatues]" + ChatColor.AQUA + "/bs create <statueName> <playerName> "+plugin.getDescription().getVersion());
                }
                break;
            case 2:
                if (sender instanceof Player && (sender.isOp() || sender.hasPermission("betterstatues.create"))){
                    betterStatues.deleteStatue((Player) sender,args[1]);
                }
                break;
            case 3:
                if (sender instanceof Player && (sender.isOp() || sender.hasPermission("betterstatues.create"))){
                   betterStatues.createPlayerModel((Player) sender,args[1],args[2]);
                }
                break;
        }
        return true;
    }

}
