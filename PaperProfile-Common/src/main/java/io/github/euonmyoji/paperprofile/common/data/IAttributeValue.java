package io.github.euonmyoji.paperprofile.common.data;

/**
 * @author yinyangshi
 */
public interface IAttributeValue {

    String getValue();

    int getValueN();

    IPaperAttribute getAttribute();

    IAttributeValue withValue(int c);

    IAttributeValue withValue(String expr);
}
