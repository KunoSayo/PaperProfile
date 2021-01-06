package io.github.euonmyoji.paperprofile.manager;


import com.google.common.collect.ImmutableMap;
import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.config.PluginConfig;
import io.github.euonmyoji.paperprofile.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinyangshi
 */
public final class LanguageManager {
    private static final Pattern ARG_PATTERN = Pattern.compile("\\{(?<f>.*?)(?<n>\\d+)}");
    private static final HashMap<String, TextTemplate> maps = new HashMap<>();
    private static ResourceBundle res;
    private static String lang;
    private static Path langFile;


    private LanguageManager() {
        throw new UnsupportedOperationException();
    }

    public static Text getText(String key, Text... texts) {
        TextTemplate textTemplate = getTemplate(key);
        ImmutableMap.Builder<String, Text> mb = ImmutableMap.builder();
        for (int i = 0; i < texts.length; i++) {
            mb.put(String.valueOf(i), texts[i]);
        }
        return textTemplate.apply(mb.build()).build();
    }

    public static Text format(String format, Text... texts) {
        TextTemplate textTemplate = getTemplate(format);
        ImmutableMap.Builder<String, Text> mb = ImmutableMap.builder();
        for (int i = 0; i < texts.length; i++) {
            mb.put(String.valueOf(i), texts[i]);
        }
        return textTemplate.apply(mb.build()).build();
    }

    public static Text getText(String key) {
        return getText(key, new Text[]{});
    }

    public static Text getText(String key, String... texts) {
        return getText(key, Arrays.stream(texts).map(Util::toText).toArray(Text[]::new));
    }

    public static void init() {
        try {
            Files.createDirectories(PluginConfig.cfgDir.resolve("lang"));
        } catch (IOException e) {
            PaperProfile.logger.warn("create lang dir error", e);
        }
        for (String lang : new String[]{"lang/zh_cn.lang"}) {
            Sponge.getAssetManager().getAsset(PaperProfile.plugin, lang)
                    .ifPresent(asset -> {
                        try {
                            asset.copyToFile(PluginConfig.cfgDir.resolve(lang));
                        } catch (IOException e) {
                            PaperProfile.logger.warn("copy language file error", e);
                        }
                    });
        }
    }

    private static void check() {
        try {
            Path langFolder = PluginConfig.cfgDir.resolve("lang");
            if (Files.notExists(langFolder)) {
                Files.createDirectory(langFolder);
            }
            try {
                if (Files.notExists(langFile)) {
                    Sponge.getAssetManager().getAsset(PaperProfile.plugin, "lang/" + lang + ".lang")
                            .orElseThrow(() -> new FileNotFoundException("asset didn't found language file!"))
                            .copyToFile(langFile);
                }
            } catch (FileNotFoundException ignore) {
                PaperProfile.logger.info("locale language file not found");
                langFile = PluginConfig.cfgDir.resolve("lang/en_US.lang");
                Sponge.getAssetManager().getAsset(PaperProfile.plugin, "lang/en_US.lang")
                        .orElseThrow(() -> new IOException("asset didn't found language file!"))
                        .copyToFile(langFile);
            }
        } catch (IOException e) {
            PaperProfile.logger.error("IOE", e);
        }
    }

    private static TextTemplate getTemplate(String key) {
        if (key == null) {
            return TextTemplate.EMPTY;
        }
        TextTemplate textTemplate = maps.get(key);
        if (textTemplate == null) {
            List<Object> objs = new ArrayList<>();
            String s = res.containsKey(key) ? res.getString(key) : key;
            Matcher matcher = ARG_PATTERN.matcher(s);
            int start = 0;
            while (matcher.find()) {
                objs.add(Util.toText(s.substring(start, matcher.start())));
                String n = matcher.group("n");
                Text f = Util.toText(matcher.group("f") + n);
                objs.add(TextTemplate.arg(n).optional().color(f.getColor())
                        .format(f.getFormat())
                        .style(f.getStyle()));
                start = matcher.end();
            }
            objs.add(Util.toText(s.substring(start)));
            //baskakbakkbakbkakbakbaskbkasbkakbakbakbakbkakbakbakbka
            //bakbakbakgbkakbakbkbk pcrepeppppppppppp
            maps.put(key, textTemplate = TextTemplate.of("{", "}", objs.toArray(new Object[0])));
        }
        return textTemplate;
    }

    public static void reload() {
        try {
            lang = PluginConfig.getUsingLang();
            langFile = PluginConfig.cfgDir.resolve("lang/" + lang + ".lang");
            check();
            maps.clear();
            res = new PropertyResourceBundle(Files.newBufferedReader(langFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            PaperProfile.logger.error("reload language file error!", e);
        }
    }
}
