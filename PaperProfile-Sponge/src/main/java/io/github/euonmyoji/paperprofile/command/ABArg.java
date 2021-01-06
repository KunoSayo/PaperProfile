package io.github.euonmyoji.paperprofile.command;

import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
@NonnullByDefault
public class ABArg extends CommandElement {
    private final boolean isBuff;

    public ABArg(boolean isBuff, String key) {
        super(Text.of(key));
        this.isBuff = isBuff;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();
        Object o = isBuff ? PaperDataConfig.buffs.get(next) : PaperDataConfig.attributes.get(next);
        if (o == null) {
            throw args.createError(LanguageManager.getText("pp.a.404", next));
        }
        return o;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (isBuff) {
            return args.nextIfPresent().map(s -> PaperDataConfig.buffs.keySet()
                    .stream().filter(s1 -> s1.startsWith(s)).collect(Collectors.toList()))
                    .orElse(new ArrayList<>(PaperDataConfig.buffs.keySet()));
        } else {
            return args.nextIfPresent().map(s -> PaperDataConfig.attributes.keySet()
                    .stream().filter(s1 -> s1.startsWith(s)).collect(Collectors.toList()))
                    .orElse(new ArrayList<>(PaperDataConfig.attributes.keySet()));

        }
    }
}
