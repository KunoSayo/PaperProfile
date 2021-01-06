package io.github.euonmyoji.paperprofile.common.data;

import java.util.List;
import java.util.Map;

/**
 * @author yinyangshi
 */
public interface IPaperBuff {
    String getKey();

    String getName();

    List<String> getLore();

    Map<String, ? extends IPaperAttribute> getValues();

    String getDisplayFormat();
}
