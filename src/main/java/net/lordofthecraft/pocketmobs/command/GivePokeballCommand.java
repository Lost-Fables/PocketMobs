package net.lordofthecraft.pocketmobs.command;

import net.lordofthecraft.pocketmobs.OrbHandler;
import net.lordofthecraft.pocketmobs.PocketMobs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GivePokeballCommand implements CommandExecutor {

    public GivePokeballCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        player.getInventory().addItem(OrbHandler.OrbGenerator(Integer.parseInt(args[0]), null));
        return true;
    }
}
