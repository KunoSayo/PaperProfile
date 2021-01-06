package io.github.euonmyoji.paperprofile.command;

import com.google.common.collect.ImmutableList;
import io.github.euonmyoji.paperprofile.common.data.ValueType;
import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
import io.github.euonmyoji.paperprofile.config.PluginConfig;
import io.github.euonmyoji.paperprofile.data.PaperAttribute;
import io.github.euonmyoji.paperprofile.data.PaperBuff;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.euonmyoji.paperprofile.PaperProfile.logger;
import static org.spongepowered.api.command.CommandResult.empty;
import static org.spongepowered.api.command.CommandResult.success;
import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public class GameMasterCommand {
    static final CommandSpec ADD = CommandSpec.builder()
            .arguments(GenericArguments.choices(of("what"), () -> ImmutableList.of("attribute", "buff"), s -> s),
                    GenericArguments.string(of("key")),
                    new ValueTypeArg(of("types")),
                    GenericArguments.optional(GenericArguments.bool(of("system"))))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    UUID uuid = ((Identifiable) src).getUniqueId();
                    if (!PluginConfig.isGm(uuid)) {
                        src.sendMessage(LanguageManager.getText("pp.pd"));
                        return empty();
                    }
                }
                String wt = args.<String>getOne("what").orElseThrow(IllegalArgumentException::new);
                String key = args.<String>getOne("key").orElseThrow(IllegalArgumentException::new);
                if (key.contains("_")) {
                    src.sendMessage(of("key doesn't match result: no '_' present."));
                    return empty();
                }

                boolean system = args.<Boolean>getOne("system").orElse(true);
                switch (wt) {
                    case "attribute": {
                        PaperAttribute paperAttribute = new PaperAttribute(key, args.<ValueType>getOne("types")
                                .orElseThrow(IllegalArgumentException::new), system);
                        try {
                            if (PaperDataConfig.addAttribute(paperAttribute)) {
                                PaperDataConfig.save();
                                src.sendMessage(LanguageManager.getText("pp.gm.att.add.suc", of(key)));
                            } else {
                                src.sendMessage(LanguageManager.getText("pp.gm.att.add.err", of(key), of("该属性已存在")));
                            }
                        } catch (ObjectMappingException e) {
                            logger.warn("add attr failed", e);
                            src.sendMessage(LanguageManager.getText("pp.gm.att.add.err", of(key), of(e)));
                        }
                        break;
                    }
                    case "buff": {
                        try {
                            PaperBuff paperAttribute = new PaperBuff(key, args.<Map<String, ValueType>>getOne("types")
                                    .orElseThrow(IllegalArgumentException::new), system);
                            if (PaperDataConfig.addBuff(paperAttribute)) {
                                PaperDataConfig.save();
                                src.sendMessage(LanguageManager.getText("pp.gm.buf.add.suc", of(key), of("该属性已存在")));
                            } else {
                                src.sendMessage(LanguageManager.getText("pp.gm.buf.add.err", of(key), of("该属性已存在")));
                            }
                        } catch (ObjectMappingException e) {
                            logger.warn("add attr failed", e);
                            src.sendMessage(LanguageManager.getText("pp.gm.att.buf.err", of(key), of(e)));
                        }
                        break;
                    }
                    default: {
                        throw new RuntimeException("?");
                    }
                }
                return success();
            })
            .build();
    static final CommandSpec DELETE = CommandSpec.builder()
            .arguments(GenericArguments.choices(of("what"), () -> ImmutableList.of("attribute", "buff"), s -> s),
                    GenericArguments.string(of("key")))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    UUID uuid = ((Identifiable) src).getUniqueId();
                    if (!PluginConfig.isGm(uuid)) {
                        src.sendMessage(LanguageManager.getText("pp.pd"));
                        return empty();
                    }
                }
                String wt = args.<String>getOne("what").orElseThrow(IllegalArgumentException::new);
                String key = args.<String>getOne("key").orElseThrow(IllegalArgumentException::new);
                if (key.contains("_")) {
                    src.sendMessage(of("key doesn't match result: no '_' present."));
                    return empty();
                }

                switch (wt) {
                    case "attribute": {
                        PaperAttribute old;
                        if ((old = PaperDataConfig.deleteAttribute(key)) != null) {
                            PaperDataConfig.save();
                            src.sendMessage(LanguageManager.getText("pp.gm.att.del.suc", old.getText()));
                        } else {
                            src.sendMessage(LanguageManager.getText("pp.gm.att.del.err", of(key), of("其不存在")));
                        }
                        break;
                    }
                    case "buff": {
                        PaperBuff old;
                        if ((old = PaperDataConfig.deleteBuff(key)) != null) {
                            PaperDataConfig.save();
                            src.sendMessage(LanguageManager.getText("pp.gm.buf.del.suc", old.getText()));
                        } else {
                            src.sendMessage(LanguageManager.getText("pp.gm.buf.del.err", of(key), of("其不存在")));
                        }
                        break;
                    }
                    default: {
                        throw new RuntimeException("?");
                    }
                }
                return success();
            })
            .build();
    private static final CommandSpec LIST = CommandSpec.builder()
            .arguments(GenericArguments.choices(of("what"), () -> ImmutableList.of("attribute", "buff"), s -> s))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    if (PluginConfig.isGm(((Identifiable) src).getUniqueId())) {
                        String what = args.<String>getOne("what").orElseThrow(IllegalArgumentException::new);
                        PaginationList.Builder builder = PaginationList.builder()
                                .title(of("属性"))
                                .padding(of("-"));
                        List<Text> textList = new ArrayList<>();
                        if ("attribute".equals(what)) {
                            for (PaperAttribute value : PaperDataConfig.attributes.values()) {
                                textList.add(value.getText());
                            }
                        } else {
                            for (PaperBuff value : PaperDataConfig.buffs.values()) {
                                textList.add(value.getText());
                            }
                        }
                        builder.contents(textList).build().sendTo(src);
                    }
                }
                return empty();
            })
            .build();
    private static final CommandSpec JOIN = CommandSpec.builder()
            .permission("paperprofile.gm.command.join")
            .arguments(GenericArguments.userOrSource(of("user")))
            .executor((src, args) -> {
                User user = args.<User>getOne(of("user")).orElseThrow(IllegalArgumentException::new);
                if (PluginConfig.addGm(user.getUniqueId())) {
                    src.sendMessage(LanguageManager.getText("pp.gm.add.suc", of(user.getName())));
                    PluginConfig.save();
                    return success();
                } else {
                    src.sendMessage(LanguageManager.getText("pp.gm.add.err", of(user.getName())));
                    return empty();
                }
            })
            .build();
    private static final CommandSpec LEAVE = CommandSpec.builder()
            .permission("paperprofile.gm.command.join")
            .arguments(GenericArguments.userOrSource(of("user")))
            .executor((src, args) -> {
                User user = args.<User>getOne(of("user")).orElseThrow(IllegalArgumentException::new);
                if (PluginConfig.deleteGm(user.getUniqueId())) {
                    src.sendMessage(LanguageManager.getText("pp.gm.rm.suc", of(user.getName())));
                    PluginConfig.save();
                    return success();
                } else {
                    src.sendMessage(LanguageManager.getText("pp.gm.rm.err", of(user.getName())));
                    return empty();
                }
            })
            .build();

    static final CommandSpec GM = CommandSpec.builder()
            .child(JOIN, "join")
            .child(LEAVE, "leave")
            .child(LIST, "list")
            .build();
}
