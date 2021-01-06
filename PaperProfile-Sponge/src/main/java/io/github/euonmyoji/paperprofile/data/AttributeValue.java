package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.data.IAttributeValue;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;

/**
 * immutable attribute value
 *
 * @author yinyangshi
 */
public class AttributeValue implements IAttributeValue {
    public final IPaperAttribute paperAttribute;
    public final String value;
    public final int vn;

    public AttributeValue(IPaperAttribute paperAttribute, String value) {
        this.paperAttribute = paperAttribute;
        if (value == null) {
            this.value = paperAttribute.getDefault();
            if (paperAttribute.getType().isNumber()) {
                vn = paperAttribute.getDefaultN();
            } else {
                this.vn = 0;
            }
        } else {
            this.value = value;
            if (paperAttribute.getType().isNumber()) {
                this.vn = Integer.parseInt(value);
            } else {
                this.vn = 0;
            }
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getValueN() {
        return vn;
    }

    @Override
    public IPaperAttribute getAttribute() {
        return paperAttribute;
    }

    @Override
    public IAttributeValue withValue(int c) {
        return new AttributeValue(paperAttribute, String.valueOf(c));
    }

    @Override
    public IAttributeValue withValue(String v) {
        return new AttributeValue(paperAttribute, v);
    }
}
