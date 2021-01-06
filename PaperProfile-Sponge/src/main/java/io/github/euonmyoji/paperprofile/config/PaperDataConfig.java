package io.github.euonmyoji.paperprofile.config;

import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.data.PaperAttribute;
import io.github.euonmyoji.paperprofile.data.PaperBuff;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinyangshi
 */
public final class PaperDataConfig {
    public static final HashMap<String, PaperAttribute> attributes = new HashMap<>();
    public static final HashMap<String, PaperBuff> buffs = new HashMap<>();
    private static final Pattern VAR_PATTERN = Pattern.compile("%(?<type>[ab])_(?<key>.*?)_(?<value>.*)%");
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private PaperDataConfig() {
        throw new UnsupportedOperationException();
    }


    public static void reload() {
        loader = HoconConfigurationLoader.builder()
                .setPath(PluginConfig.dataDir.resolve("info.conf")).build();
        loadNode();
        try {
            Files.createDirectories(PluginConfig.dataDir.resolve("player"));
        } catch (IOException e) {
            PaperProfile.logger.warn("create player dir failed", e);
        }

        attributes.clear();
        buffs.clear();
        cfg.getNode("attributes").getChildrenMap().forEach((o, node) -> {
            String key = o.toString();
            if (!attributes.containsKey(key)) {
                try {
                    attributes.put(key, new PaperAttribute(key, node));
                } catch (ObjectMappingException e) {
                    PaperProfile.logger.warn("load attribute " + key + " failed.", e);
                }
            }
        });

        cfg.getNode("buffs").getChildrenMap().forEach((o, node) -> {
            String key = o.toString();
            try {
                buffs.put(key, new PaperBuff(key, node));
            } catch (ObjectMappingException e) {
                PaperProfile.logger.warn("load attribute " + key + " failed.", e);
            }
        });

        save();
    }


    public static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            PaperProfile.logger.warn("error when saving plugin config", e);
        }
    }

    private static void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        } catch (IOException e) {
            PaperProfile.logger.warn("load plugin config failed, creating new one", e);
            cfg = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public static PaperAttribute acquireAttribute(String key) throws ObjectMappingException {
        PaperAttribute attribute = attributes.get(key);
        if (attribute == null) {
            attributes.put(key, attribute = new PaperAttribute(key, cfg.getNode("attributes", key)));
        }
        return attribute;
    }

    public static boolean addAttribute(PaperAttribute paperAttribute) throws ObjectMappingException {
        if (attributes.containsKey(paperAttribute.key)) {
            return false;
        }
        attributes.put(paperAttribute.key, paperAttribute);
        paperAttribute.saveTo(cfg.getNode("attribute", paperAttribute.key));
        return true;
    }

    public static boolean addBuff(PaperBuff buff) throws ObjectMappingException {
        if (buffs.containsKey(buff.key)) {
            return false;
        }
        buffs.put(buff.key, buff);
        buff.saveTo(cfg.getNode("buffs", buff.key));
        return true;
    }

    public static PaperAttribute deleteAttribute(String key) {
        PaperAttribute t = attributes.remove(key);
        cfg.getNode("attributes").removeChild(key);
        return t;
    }

    public static String parse(IPlayerConfig playerConfig, String v) {
        Matcher matcher = VAR_PATTERN.matcher(v);
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            sb.append(v, start, matcher.start());
            String type = matcher.group("type");
            String key = matcher.group("key");
            String value = matcher.group("value");
            start = matcher.end();
            if (type.equals("a")) {
                //%a_hp_value%
                switch (value) {
                    case "max": {
                        sb.append(attributes.get(key).max.get(playerConfig));
                        break;
                    }
                    case "min": {
                        sb.append(attributes.get(key).min.get(playerConfig));
                        break;
                    }
                    case "default": {
                        sb.append(attributes.get(key).def);
                        break;
                    }
                    case "value": {
                        sb.append(playerConfig.getAttribute(attributes.get(key)).getValue());
                        break;
                    }
                    default: {
                        sb.append(v, matcher.start(), matcher.end());
                        break;
                    }
                }
            } else {
                //%b_key_name-key-value%
                String[] args = value.split("-", 3);
                if (args.length <= 2) {
                    sb.append(v, matcher.start(), matcher.end());
                } else {
                    PaperAttribute attribute = buffs.get(key).values.get(args[1]);
                    if (attribute == null) {
                        sb.append(v, matcher.start(), matcher.end());
                    } else {
                        switch (args[2]) {
                            case "max": {
                                sb.append(attribute.max.get(playerConfig));
                                break;
                            }
                            case "min": {
                                sb.append(attribute.min.get(playerConfig));
                                break;
                            }
                            case "default": {
                                sb.append(attribute.def);
                                break;
                            }
                            case "value": {
                                sb.append(playerConfig.getAttribute(attribute).getValue());
                                break;
                            }
                            default: {
                                sb.append(v, matcher.start(), matcher.end());
                                break;
                            }
                        }
                    }
                }
            }
        }
        sb.append(v.substring(start));
        return sb.toString();
    }

    public static PaperBuff deleteBuff(String key) {
        PaperBuff buf = buffs.remove(key);
        cfg.getNode("buffs").removeChild(key);
        return buf;
    }
}
