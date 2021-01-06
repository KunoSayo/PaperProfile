package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.IOptionNumber;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;
import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * @author yinyangshi
 */
public class OptionNumber implements IOptionNumber {
    private final IPaperAttribute attribute;
    private boolean isConst = false;
    private int c = 0;

    public OptionNumber(int n) {
        attribute = null;
        isConst = true;
        c = n;
    }

    public OptionNumber(String v) throws ObjectMappingException {
        try {
            c = Integer.parseInt(v);
            isConst = true;
        } catch (NumberFormatException ignore) {

        }
        attribute = isConst ? null : PaperDataConfig.acquireAttribute(v);
    }

    @Override
    public int get(IPlayerConfig value) {
        return isConst ? c : value.getAttribute(attribute).getValueN();
    }

    void saveTo(CommentedConfigurationNode node) {
        if (isConst) {
            node.setValue(c);
        } else {
            node.setValue(attribute.getKey());
        }
    }
}
