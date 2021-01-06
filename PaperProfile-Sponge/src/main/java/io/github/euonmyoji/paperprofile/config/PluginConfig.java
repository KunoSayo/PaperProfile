package io.github.euonmyoji.paperprofile.config;

import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class PluginConfig {
    private static final HashMap<UUID, IPlayerConfig> playerConfigs = new HashMap<>();

    private static final String DATA_DIR = "data-dir-path";
    private static final String LANGUAGE = "lang";
    public static CommentedConfigurationNode cfg;
    public static CommentedConfigurationNode generalNode;
    public static Path dataDir;
    public static Path cfgDir;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private PluginConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        cfgDir = PaperProfile.defCfgDir;
        loader = HoconConfigurationLoader.builder()
                .setPath(cfgDir.resolve("config.conf")).build();
        LanguageManager.init();
        reload();
        save();

    }

    public static void reload() {
        loadNode();

        String path = generalNode.getNode(DATA_DIR).getString("default");
        generalNode.getNode(LANGUAGE).getString(Locale.SIMPLIFIED_CHINESE.toString());

        dataDir = "default".equals(path) ? cfgDir : Paths.get(path);
        PaperProfile.logger.info("using data dir path:" + dataDir);

        LanguageManager.reload();
        PaperDataConfig.reload();
    }

    public static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            PaperProfile.logger.warn("error when saving plugin config", e);
        }
    }

    public static boolean addGm(UUID uuid) {
        IPlayerConfig config = getPlayerConfig(uuid);
        if (config.isGm()) {
            return false;
        } else {
            config.setGm(true);
            config.save();
            return true;
        }
    }

    public static boolean deleteGm(UUID uuid) {
        IPlayerConfig config = getPlayerConfig(uuid);
        if (config.isGm()) {
            config.setGm(false);
            config.save();
            return true;
        } else {
            return false;
        }
    }

    private static void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        } catch (IOException e) {
            PaperProfile.logger.warn("load plugin config failed, creating new one", e);
            cfg = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        generalNode = cfg.getNode("general");
    }


    public static String getUsingLang() {
        return generalNode.getNode(LANGUAGE).getString(Locale.SIMPLIFIED_CHINESE.toString().toLowerCase());
    }

    public static boolean isGm(UUID uuid) {
        return playerConfigs.getOrDefault(uuid, IPlayerConfig.EMPTY).isGm();
    }

    public static IPlayerConfig getPlayerConfig(UUID uuid) {
        IPlayerConfig playerConfig = playerConfigs.get(uuid);
        if (playerConfig == null) {
            playerConfigs.put(uuid, playerConfig = new LocalPlayerConfig(uuid));
        }
        return playerConfig;
    }

    public static void removePlayerCache(UUID uuid) {
        playerConfigs.remove(uuid);
    }
}
