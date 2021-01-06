package io.github.euonmyoji.paperprofile.bukkit;

import io.github.euonmyoji.paperprofile.common.DiceException;
import io.github.euonmyoji.paperprofile.common.DiceExpression;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author yinyangshi
 */
public class PaperProfile extends JavaPlugin {
    public static final String VERSION = "@pluginVersion@";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            try {
                var dice = new DiceExpression(args[0]);
                Bukkit.getServer().broadcastMessage(sender.getName() + "掷骰: "
                        + dice.getRaw() + "=" + dice.toString() + "=" + dice.getTotalResult());
                return true;
            } catch (DiceException e) {
                sender.sendMessage(e.getTip());
                sender.sendMessage(e.getMessage());
            }
        }
        return false;
    }
}
