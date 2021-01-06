package io.github.euonmyoji.paperprofile;

import com.google.inject.Inject;
import io.github.euonmyoji.paperprofile.command.Command;
import io.github.euonmyoji.paperprofile.config.PluginConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author yinyangshi
 */
@Plugin(id = "paperprofile", name = "PaperProfile", version = PaperProfile.VERSION,
        authors = "yinyangshi", description = "Player's profile in paper.")
public class PaperProfile {
    public static final String VERSION = "@pluginVersion@";
    public static Logger logger;
    public static PaperProfile plugin;

    public static Path defCfgDir;

    @Inject
    public PaperProfile(Logger logger, @ConfigDir(sharedRoot = false) Path dir) {
        PaperProfile.logger = logger;
        plugin = this;
        defCfgDir = dir;
    }


    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        try {
            Files.createDirectories(defCfgDir);
            PluginConfig.init();
        } catch (IOException e) {
            logger.warn("init plugin IOE!", e);
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, Command.PAPER_PROFILE, "paperprofile", "pp");
        Sponge.getCommandManager().register(this, Command.DICE, "r", "rd", "roll", "rolldice");
        Sponge.getCommandManager().register(this, Command.DICE_HIDE, "rh", "rollhide");
        Sponge.getCommandManager().register(this, Command.NN, "nn", "name");
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        Task.builder().async().execute(() -> PluginConfig.getPlayerConfig(player.getUniqueId()).init(player.getName())).submit(this);
    }
}
