package com.mcbans.firestar.mcbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.callBacks.ManualResync;
import com.mcbans.firestar.mcbans.callBacks.ManualSync;
import com.mcbans.firestar.mcbans.callBacks.Ping;
import com.mcbans.firestar.mcbans.callBacks.serverChoose;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandMcbans extends BaseCommand{
    public CommandMcbans(){
        bePlayer = false;
        name = "mcbans";
        argLength = 0;
        usage = "show information";
        banning = false;
    }

    @Override
    public void execute() throws CommandException {
        /* General Help */
        if (args.size() == 0){
            send(ChatColor.BLUE + "MCBans Help");
            send(ChatColor.WHITE + "/mcbans banning" + ChatColor.BLUE + " Help with banning/unban command");
            send(ChatColor.WHITE + "/mcbans get" + ChatColor.BLUE + " Get time till next call");
            send(ChatColor.WHITE + "/mcbans ping" + ChatColor.BLUE + " Check overall response time from API");
            send(ChatColor.WHITE + "/mcbans reload" + ChatColor.BLUE + " Reload settings and language file");
            send(ChatColor.WHITE + "/mcbans sync" + ChatColor.BLUE + " Force a sync to occur");
            send(ChatColor.WHITE + "/mcbans user" + ChatColor.BLUE + " Help with user management commands");
            return;
        }

        final String first = args.remove(0);
        /* Banning Help */
        if (first.equalsIgnoreCase("banning")){
            send(ChatColor.WHITE + "/ban <name> <reason>" + ChatColor.BLUE + " Local ban user");
            send(ChatColor.WHITE + "/ban <name> g <reason>" + ChatColor.BLUE + " Global ban user");
            send(ChatColor.WHITE + "/ban <name> t <time> <m or h or d> <reason>" + ChatColor.BLUE + " Temporarily ban");
            send(ChatColor.WHITE + "/tban <name> <time> <m(minute) or h(hour) or d(day), w(week)> <reason>" + ChatColor.BLUE + " Temp ban user");
            send(ChatColor.WHITE + "/gban <name> <reason>" + ChatColor.BLUE + " Global ban user");
            send(ChatColor.WHITE + "/rban <name> <reason>" + ChatColor.BLUE + " Rollback and local ban");
            send(ChatColor.WHITE + "/rban <name> g <reason>" + ChatColor.BLUE + " Rollback and global ban");
            send(ChatColor.WHITE + "/rban <name> t <time> <m or h or d> <reason>" + ChatColor.BLUE + " Rollback and temporarily ban");
            return;
        }
        /* User Help */
        if (first.equalsIgnoreCase("user")){
            send(ChatColor.WHITE + "/lookup <name>" + ChatColor.BLUE + " Lookup the reputation information");
            send(ChatColor.WHITE + "/kick <name> <reason>" + ChatColor.BLUE + " Kick user from the server");
            return;
        }
        /* Check response time */
        if (first.equalsIgnoreCase("ping")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.DARK_RED + plugin.language.getFormat("permissionDenied"));
            }
            Ping manualPingCheck = new Ping(plugin, senderName);
            (new Thread(manualPingCheck)).start();
            return;
        }
        /* Sync banned-players.txt */
        if (first.equalsIgnoreCase("sync")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.DARK_RED + plugin.language.getFormat("permissionDenied"));
            }

            // Check if all sync
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("all")){
                send(ChatColor.GREEN + " Re-Sync has started!");
                ManualResync manualSyncBanRunner = new ManualResync(plugin, senderName);
                (new Thread(manualSyncBanRunner)).start();
            }else{
                long syncInterval = config.getInteger("syncInterval");
                if(syncInterval < (60 * 5)){ // minimum 5 minutes
                    syncInterval = 60 * 5;
                }
                long ht = (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000);
                if (ht > 10) {
                    send(ChatColor.GREEN + " Sync has started!");
                    ManualSync manualSyncBanRunner = new ManualSync(plugin, senderName);
                    (new Thread(manualSyncBanRunner)).start();
                } else {
                    throw new CommandException(ChatColor.RED + "[Unable] Sync will occur in less than 10 seconds!");
                }
            }
            return;
        }
        /* Get next scheduling time */
        if (first.equalsIgnoreCase("get")){
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("call")){
                long callBackInterval = 0;
                callBackInterval = 60 * config.getInteger("callBackInterval");
                if(callBackInterval < (60 * 15)){
                    callBackInterval = (60 * 15);
                }
                String r = this.timeRemain( (plugin.lastCallBack + callBackInterval) - (System.currentTimeMillis() / 1000) );
                send(ChatColor.GOLD + r + " until next callback request.");
            }
            else if (args.size() > 0 && args.get(0).equalsIgnoreCase("sync")){
                long syncInterval = config.getInteger("syncInterval");
                if(syncInterval < (60 * 5)){
                    syncInterval = (60 * 5);
                }
                String r = this.timeRemain( (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000) );
                send(ChatColor.GOLD + r + " until next sync.");
            }
            else{
                send(ChatColor.WHITE + "/mcbans get call" + ChatColor.BLUE + " Time until callback thread sends data.");
                send(ChatColor.WHITE + "/mcbans get sync" + ChatColor.BLUE + " Time until next sync.");
            }
            return;
        }
        /* Reload plugin */
        if (first.equalsIgnoreCase("reload")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.DARK_RED + plugin.language.getFormat("permissionDenied"));
            }

            send(ChatColor.AQUA + "Reloading Settings..");
            Integer reloadSettings = plugin.settings.reload();
            if (reloadSettings == -2) {
                send(ChatColor.RED + "Reload failed - File missing!");
            } else if (reloadSettings == -1) {
                send(ChatColor.RED + "Reload failed - File integrity failed!");
            } else {
                send(ChatColor.GREEN + "Reload completed!");
            }
            send(ChatColor.AQUA + "Reloading Language File..");
            boolean reloadLanguage = plugin.language.reload();
            if (!reloadLanguage) {
                send(ChatColor.RED + "Reload failed - File missing!");
            } else {
                send(ChatColor.GREEN + "Reload completed!");
            }
            serverChoose serverChooser = new serverChoose(plugin);
            (new Thread(serverChooser)).start();
            return;
        }

        // Format error
        throw new CommandException(ChatColor.DARK_RED + plugin.language.getFormat("formatError"));
    }

    private void send(final String msg){
        Util.message(sender, msg);
    }

    private String timeRemain(long remain) {
        try {
            String format = "";
            long timeRemaining = remain;
            long sec = timeRemaining % 60;
            long min = (timeRemaining / 60) % 60;
            long hours = (timeRemaining / (60 * 60)) % 24;
            long days = (timeRemaining / (60 * 60 * 24)) % 7;
            long weeks = (timeRemaining / (60 * 60 * 24 * 7));
            if (sec != 0) {
                format = sec + " seconds";
            }
            if (min != 0) {
                format = min + " minutes " + format;
            }
            if (hours != 0) {
                format = hours + " hours " + format;
            }
            if (days != 0) {
                format = days + " days " + format;
            }
            if (weeks != 0) {
                format = weeks + " weeks " + format;
            }
            return format;
        } catch (ArithmeticException e) {
            return "";
        }
    }

    @Override
    public boolean permission(CommandSender sender) {
        return true;
    }
}
