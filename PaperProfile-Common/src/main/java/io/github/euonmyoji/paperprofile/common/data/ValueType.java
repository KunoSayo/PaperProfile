package io.github.euonmyoji.paperprofile.common.data;

/**
 * @author yinyangshi
 */
public enum ValueType {
    STRING,
    NUMBER;

    public boolean isNumber() {
        return this == NUMBER;
    }
}
