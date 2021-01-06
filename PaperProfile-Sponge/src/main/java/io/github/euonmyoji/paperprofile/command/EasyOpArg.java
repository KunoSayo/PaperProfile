package io.github.euonmyoji.paperprofile.command;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yinyangshi
 */
@NonnullByDefault
public class EasyOpArg extends CommandElement {
    private static final List<String> list = ImmutableList.of("+", "-", "=");

    public EasyOpArg(String op) {
        super(Text.of(op));
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();
        switch (next) {
            case "+":
            case "-":
            case "=": {
                return next;
            }
            default: {
                throw args.createError(Text.of("Need operator, not " + next));
            }
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return list;
    }
}
