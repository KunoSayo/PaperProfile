package io.github.euonmyoji.paperprofile.config;

import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.IAttributeValue;
import io.github.euonmyoji.paperprofile.common.data.IBuffValue;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;
import io.github.euonmyoji.paperprofile.common.data.IPaperBuff;
import io.github.euonmyoji.paperprofile.data.AttributeValue;
import io.github.euonmyoji.paperprofile.data.BuffValue;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class LocalPlayerConfig implements IPlayerConfig {
    private final UUID uuid;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode cfg;

    public LocalPlayerConfig(UUID uuid) {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder()
                .setPath(PluginConfig.dataDir.resolve("player").resolve(uuid + ".conf")).build();
        loadNode();
    }

    @Override
    public String getName() {
        return cfg.getNode("name").getString(uuid.toString());
    }

    @Override
    public LocalPlayerConfig setName(String name) {
        cfg.getNode("name").setValue(name);
        return this;
    }

    @Override
    public void init(String mcName) {
        if (cfg.getNode("name").isVirtual()) {
            cfg.getNode("name").setValue(mcName);
        }
        save();
    }

    @Override
    public AttributeValue getAttribute(IPaperAttribute attribute) {
        return new AttributeValue(attribute, cfg.getNode(attribute.getKey()).getString(attribute.getDefault()));
    }

    @Override
    public void setAttribute(IAttributeValue attributeValue) {
        cfg.getNode(attributeValue.getAttribute().getKey()).setValue(attributeValue.getValue());
    }

    @Override
    public BuffValue getBuff(IPaperBuff buff) {
        if (cfg.getNode(buff.getKey()).isVirtual()) {
            return null;
        } else {
            return new BuffValue(buff, cfg.getNode(buff.getKey()));
        }
    }

    @Override
    public void setBuff(IBuffValue buff) {

    }

    @Override
    public boolean isGm() {
        return cfg.getNode("gm").getBoolean(false);
    }

    @Override
    public void setGm(boolean gm) {
        cfg.getNode("gm").setValue(gm);
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            PaperProfile.logger.warn("error when saving plugin config", e);
        }
        return false;
    }

    @Override
    public boolean hasBuff(IPaperBuff buff) {
        return !cfg.getNode(buff.getKey()).isVirtual();
    }

    private void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        } catch (IOException e) {
            PaperProfile.logger.warn("load plugin config failed, creating new one", e);
            cfg = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

}
