package io.github.euonmyoji.paperprofile.command;

import com.google.common.collect.ImmutableMap;
import io.github.euonmyoji.paperprofile.PaperProfile;
import io.github.euonmyoji.paperprofile.common.DiceException;
import io.github.euonmyoji.paperprofile.common.DiceExpression;
import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.IAttributeValue;
import io.github.euonmyoji.paperprofile.config.PluginConfig;
import io.github.euonmyoji.paperprofile.data.PaperAttribute;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Identifiable;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static io.github.euonmyoji.paperprofile.PaperProfile.plugin;
import static io.github.euonmyoji.paperprofile.command.GameMasterCommand.*;
import static io.github.euonmyoji.paperprofile.util.Util.toText;
import static org.spongepowered.api.command.CommandResult.empty;
import static org.spongepowered.api.command.CommandResult.success;
import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public class Command {
    public static final CommandSpec A = CommandSpec.builder()
            .arguments(new ABArg(false, "attribute"), new EasyOpArg("op"), GenericArguments.string(of("expr")),
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(of("msg"))))
            .executor((src, args) -> {
                if (src instanceof Identifiable) {
                    Task.builder().async().execute(() -> {
                        PaperAttribute paperAttribute = args.<PaperAttribute>getOne("attribute")
                                .orElseThrow(IllegalArgumentException::new);
                        String expr = args.<String>getOne("expr")
                                .orElseThrow(IllegalArgumentException::new);
                        IPlayerConfig playerConfig = PluginConfig.getPlayerConfig(((Identifiable) src).getUniqueId());
                        Optional<String> reason = args.getOne("msg");
                        String op = args.<String>getOne("op")
                                .orElseThrow(IllegalArgumentException::new);
                        if (paperAttribute.type.isNumber()) {
                            try {
                                DiceExpression diceExpression = new DiceExpression(expr);
                                IAttributeValue oldValue = playerConfig.getAttribute(paperAttribute);
                                int a = oldValue.getValueN();
                                int c;
                                Text text;
                                switch (op) {
                                    case "-": {
                                        c = a - diceExpression.getTotalResult();
                                        text = LanguageManager.getText("pp.a.sub" + (reason.isPresent() ? ".reason" : ""),
                                                toText(playerConfig.getName()), paperAttribute.getText(), of(a), of(c), of(expr),
                                                of(diceExpression.toString()), of(diceExpression.getTotalResult())
                                                , toText(reason.orElse("")));
                                        break;
                                    }
                                    case "=": {
                                        c = diceExpression.getTotalResult();
                                        text = LanguageManager.getText("pp.a.set" + (reason.isPresent() ? ".reason" : ""),
                                                of(playerConfig.getName()), paperAttribute.getText(), of(a), of(c), of(expr),
                                                of(diceExpression.toString()), of(diceExpression.getTotalResult())
                                                , toText(reason.orElse("")));
                                        break;
                                    }
                                    default: {
                                        c = a + diceExpression.getTotalResult();
                                        text = LanguageManager.getText("pp.a.add" + (reason.isPresent() ? ".reason" : ""),
                                                of(playerConfig.getName()), paperAttribute.getText(), of(a), of(c), of(expr),
                                                of(diceExpression.toString()), of(diceExpression.getTotalResult())
                                                , toText(reason.orElse("")));
                                        break;
                                    }
                                }
                                Sponge.getServer().getBroadcastChannel().send(text);
                                playerConfig.setAttribute(oldValue.withValue(c));
                                playerConfig.save();
                            } catch (DiceException e) {
                                src.sendMessage(LanguageManager.getText(e.getTip(), expr, e.args));
                            }
                        } else {
                            if (!"=".equals(op)) {
                                src.sendMessage(of("operator must be '=' for string"));
                                return;
                            }
                            IAttributeValue oldValue = playerConfig.getAttribute(paperAttribute);
                            Text text = LanguageManager.getText("pp.a.s.set" + (reason.isPresent() ? ".reason" : ""),
                                    toText(playerConfig.getName()), paperAttribute.getText(), toText(oldValue.getValue()), toText(expr),
                                    toText(reason.orElse("")));
                            Sponge.getServer().getBroadcastChannel().send(text);
                            playerConfig.setAttribute(oldValue.withValue(expr));
                            playerConfig.save();
                        }
                    }).submit(plugin);
                    return success();
                }
                return empty();
            })
            .build();
    public static final CommandSpec PAPER_PROFILE = CommandSpec.builder()
            .executor((src, args) -> {
                Text prefix = toText("&ePaperProfile v.");
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
            .child(A, "a", "st")
            .child(ShowProfileCommand.SHOW, "show")
            .build();
    private static final HashMap<String, Task> commanded = new HashMap<>();
    public static final CommandSpec DICE = CommandSpec.builder()
            .arguments(new DiceArg(of("expr")),
                    GenericArguments.optional(GenericArguments.remainingJoinedStrings(of("reason"))))
            .executor((src, args) -> {
                if (isSpam(src)) {
                    return empty();
                }
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
                if (isSpam(src)) {
                    return empty();
                }
                if (src instanceof Identifiable) {
                    IPlayerConfig playerConfig = PluginConfig.getPlayerConfig(((Identifiable) src).getUniqueId());
                    DiceExpression expr = args.<DiceExpression>getOne("expr").orElseThrow(IllegalArgumentException::new);
                    Optional<String> reason = args.getOne("reason");
                    Sponge.getServer().getBroadcastChannel().send(LanguageManager
                            .getText("pp.dice.hide", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult())));
                    Text toGm = reason.map(s -> LanguageManager
                            .getText("pp.dice.hide.gm.reason", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult()), s)).orElseGet(() -> LanguageManager
                            .getText("pp.dice.hide.gm", playerConfig.getName(), expr.getRaw(), expr.toString(),
                                    String.valueOf(expr.getTotalResult())));
                    if (!sendToGm(((Identifiable) src).getUniqueId(), toGm)) {
                        src.sendMessage(toGm);
                    }
                    return success();
                } else {
                    return empty();
                }
            })
            .build();
    public static final CommandSpec NN = CommandSpec.builder()
            .arguments(GenericArguments.userOrSource(of("user")), GenericArguments.string(of("name")))
            .executor((src, args) -> {
                if (isSpam(src)) {
                    return empty();
                }
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

    private static boolean sendToGm(UUID src, Text text) {
        boolean sentSrc = false;
        for (Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
            if (PluginConfig.isGm(onlinePlayer.getUniqueId())) {
                onlinePlayer.sendMessage(text);
                sentSrc |= onlinePlayer.getUniqueId().equals(src);
            }
        }
        return sentSrc;
    }

    static boolean isSpam(CommandSource src) {
        Task task = commanded.get(src.getName());
        if (task == null) {
            commanded.put(src.getName(), Task.builder().execute(() -> commanded.remove(src.getName())).delayTicks(20).submit(plugin));
            return false;
        } else {
            task.cancel();
            src.sendMessage(LanguageManager.getText("pp.spam"));
            commanded.put(src.getName(), Task.builder().execute(() -> commanded.remove(src.getName())).delayTicks(20).submit(plugin));
            return true;
        }
    }
}
