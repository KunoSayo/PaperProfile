package io.github.euonmyoji.paperprofile.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.common.DiceExpression;
import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.ValueType;
import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
import io.github.euonmyoji.paperprofile.config.PluginConfig;
import io.github.euonmyoji.paperprofile.data.PaperAttribute;
import io.github.euonmyoji.paperprofile.data.PaperBuff;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import io.github.euonmyoji.paperprofile.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Identifiable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.github.euonmyoji.paperprofile.PaperProfile.logger;
import static org.spongepowered.api.command.CommandResult.empty;
import static org.spongepowered.api.command.CommandResult.success;
import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public class Command {

    public static final CommandSpec DICE = CommandSpec.builder()
            .arguments(new DiceArg(of("expr")),
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(of("reason"))))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    IPlayerConfig playerConfig = PluginConfig.getPlayerConfig(((Identifiable) src).getUniqueId());
                    DiceExpression expr = args.<DiceExpression>getOne("expr").orElseThrow(IllegalArgumentException::new);
                    Optional<String> reason = args.getOne("reason");
                    Sponge.getServer().getBroadcastChannel().send(LanguageManager
                            .getText(expr.getMsgNode(reason.isPresent()), playerConfig.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult()), reason.orElse("")));
                    return success();
                } else {
                    DiceExpression expr = args.<DiceExpression>getOne("expr").orElseThrow(IllegalArgumentException::new);
                    Optional<String> reason = args.getOne("reason");
                    Sponge.getServer().getBroadcastChannel().send(LanguageManager
                            .getText(expr.getMsgNode(reason.isPresent()), src.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult()), reason.orElse("")));
                    return empty();
                }
            })
            .build();
    public static final CommandSpec DICE_HIDE = CommandSpec.builder()
            .arguments(new DiceArg(of("expr")),
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(of("reason"))))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    IPlayerConfig playerConfig = PluginConfig.getPlayerConfig(((Identifiable) src).getUniqueId());
                    DiceExpression expr = args.<DiceExpression>getOne("expr").orElseThrow(IllegalArgumentException::new);
                    Optional<String> reason = args.getOne("reason");
                    Sponge.getServer().getBroadcastChannel().send(LanguageManager
                            .getText("pp.dice.hide", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult())));
                    Text toGm;
                    if (reason.isPresent()) {
                        toGm = LanguageManager
                                .getText("pp.dice.hide.gm.reason", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                        String.valueOf(expr.getTotalResult()), reason.get());
                    } else {
                        toGm = LanguageManager
                                .getText("pp.dice.hide.gm", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                        String.valueOf(expr.getTotalResult()));
                    }
                    sendToGm(toGm);
                    return success();
                } else {
                    return empty();
                }
            })
            .build();
    private static final CommandSpec ADD = CommandSpec.builder()
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
    private static final CommandSpec DELETE = CommandSpec.builder()
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


    private static final CommandSpec GM = CommandSpec.builder()
            .child(JOIN, "join")
            .child(LEAVE, "leave")
            .build();
    public static final CommandSpec PAPER_PROFILE = CommandSpec.builder()
            .executor((src, args) -> {
                Text prefix = Util.toText("&ePaperProfile v.");
                TextTemplate textTemplate = TextTemplate.of(prefix, TextTemplate.arg("version"));
                //show version no
                //test template yes
                src.sendMessage(textTemplate.apply(ImmutableMap.of("version", PaperProfile.VERSION)).build());
                return success();
            })
            .child(GM, "gm")
            .child(DICE, "r", "rd", "roll")
            .child(DICE_HIDE, "rh", "roll_hide")
            .child(ADD, "add")
            .child(DELETE, "delete")
            .build();

    public static CommandSpec NN = CommandSpec.builder()
            .arguments(GenericArguments.userOrSource(of("user")), GenericArguments.string(of("name")))
            .executor((src, args) -> {
                User user = args.<User>getOne("user").orElseThrow(IllegalArgumentException::new);
                String name = args.<String>getOne("name").orElseThrow(IllegalArgumentException::new);
                if (user.getName().equals(src.getName()) || (src instanceof Identifiable && PluginConfig.isGm(((Identifiable) src).getUniqueId()))) {
                    IPlayerConfig playerConfig = PluginConfig.getPlayerConfig(user.getUniqueId());
                    String old = playerConfig.getName();
                    if (playerConfig.setName(name).save()) {
                        src.sendMessage(LanguageManager.getText("pp.nn", old, name));
                    } else {
                        src.sendMessage(LanguageManager.getText("pp.nn.err", old, name));
                    }
                    return success();
                } else {
                    src.sendMessage(LanguageManager.getText("pp.pd"));
                }
                return empty();
            })
            .build();

    private static void sendToGm(Text text) {
        for (Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
            if (PluginConfig.isGm(onlinePlayer.getUniqueId())) {
                onlinePlayer.sendMessage(text);
            }
        }
    }
}
