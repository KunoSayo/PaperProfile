package io.github.euonmyoji.paperprofile.common.config;


import io.github.euonmyoji.paperprofile.common.data.IAttributeValue;
import io.github.euonmyoji.paperprofile.common.data.IBuffValue;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;
import io.github.euonmyoji.paperprofile.common.data.IPaperBuff;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public interface IPlayerConfig {

    IPlayerConfig EMPTY = new IPlayerConfig() {
        @Override
        public String getName() {
            return "&null";
        }

        @Override
        public IPlayerConfig setName(String name) {
            return this;
        }

        @Override
        public void init(String mcName) {

        }

        @Override
        public IAttributeValue getAttribute(IPaperAttribute attribute) {
            return null;
        }

        @Override
        public void setAttribute(IAttributeValue IAttributeValue) {

        }

        @Override
        public IBuffValue getBuff(IPaperBuff buff) {
            return null;
        }

        @Override
        public void setBuff(IBuffValue buff) {

        }

        @Override
        public boolean isGm() {
            return false;
        }

        @Override
        public void setGm(boolean gm) {

        }

        @Override
        public UUID getUUID() {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }

        @Override
        public boolean save() {
            return false;
        }
    };

    String getName();

    IPlayerConfig setName(String name);

    void init(String mcName);

    IAttributeValue getAttribute(IPaperAttribute attribute);

    void setAttribute(IAttributeValue IAttributeValue);

    IBuffValue getBuff(IPaperBuff buff);

    void setBuff(IBuffValue buff);

    boolean isGm();

    void setGm(boolean gm);

    UUID getUUID();

    boolean save();
}
