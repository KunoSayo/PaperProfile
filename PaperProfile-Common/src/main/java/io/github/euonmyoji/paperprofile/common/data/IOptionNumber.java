package io.github.euonmyoji.paperprofile.common.data;

import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;

/**
 * @author yinyangshi
 */
public interface IOptionNumber {
    int get(IPlayerConfig value);

    String getInfo(IPlayerConfig playerConfig);
}
