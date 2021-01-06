package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.data.IBuffValue;
import io.github.euonmyoji.paperprofile.common.data.IPaperBuff;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.HashMap;

/**
 * @author yinyangshi
 */
public class BuffValue implements IBuffValue {
    public final IPaperBuff paperBuff;
    public final HashMap<String, AttributeValue> values;

    public BuffValue(PaperBuff paperBuff, HashMap<String, AttributeValue> values) {
        this.paperBuff = paperBuff;
        this.values = values;
    }

    public BuffValue(PaperBuff buff) {
        this.paperBuff = buff;
        this.values = new HashMap<>();
        buff.values.forEach((s, paperAttribute) -> this.values.put(s, new AttributeValue(paperAttribute, paperAttribute.def)));
    }

    public BuffValue(IPaperBuff buff, CommentedConfigurationNode node) {
        this.paperBuff = buff;
        values = new HashMap<>();
        buff.getValues().forEach((s, attribute) -> values.put(s, new AttributeValue(attribute, node.getNode(attribute.getKey()).getString())));
    }
}
