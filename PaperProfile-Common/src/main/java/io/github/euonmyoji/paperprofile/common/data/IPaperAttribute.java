package io.github.euonmyoji.paperprofile.common.data;

import java.util.List;

/**
 * @author yinyangshi
 */
public interface IPaperAttribute {

    String getKey();

    String getName();

    List<String> getLore();

    ValueType getType();

    String getDefault();

    default int getDefaultN() {
        return Integer.parseInt(getDefault());
    }

    String getDisplayFormat();

    IOptionNumber getMin();

    IOptionNumber getMax();
}
