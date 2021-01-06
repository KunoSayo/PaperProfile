package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.data.IAttributeValue;

/**
 * immutable attribute value
 *
 * @author yinyangshi
 */
public class AttributeValue implements IAttributeValue {
    public final PaperAttribute paperAttribute;
    public final String value;
    public final int vn;

    public AttributeValue(PaperAttribute paperAttribute, String value) {
        this.paperAttribute = paperAttribute;
        this.value = value;
        if (paperAttribute.type.isNumber()) {
            this.vn = Integer.parseInt(value);
        } else {
            this.vn = 0;
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
}
