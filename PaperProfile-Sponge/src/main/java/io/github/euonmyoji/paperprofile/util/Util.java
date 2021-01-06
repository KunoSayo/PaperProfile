package io.github.euonmyoji.paperprofile.util;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public final class Util {
    public static final TypeToken<List<String>> STRING_LIST_TYPE = new TypeToken<List<String>>() {
    };

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public static String toStr(Text text) {
        return TextSerializers.FORMATTING_CODE.serialize(text);
    }
}
