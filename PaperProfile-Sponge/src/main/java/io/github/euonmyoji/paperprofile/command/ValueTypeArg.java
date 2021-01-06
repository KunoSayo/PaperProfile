package io.github.euonmyoji.paperprofile.command;

import io.github.euonmyoji.paperprofile.common.data.ValueType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
@NonnullByDefault
public class ValueTypeArg extends CommandElement {
    protected ValueTypeArg(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();
        try {
            if (next.contains("_")) {
                String[] values = next.split(",");
                return Arrays.stream(values).map(s -> s.split("_", 2))
                        .collect(Collectors.toMap(strings -> strings[0], strings -> ValueType.valueOf(strings[1].toUpperCase())));
            } else {
                return ValueType.valueOf(next.toUpperCase());
            }
        } catch (Exception ee) {
            ArgumentParseException e = args.createError(Text.of("cannot be parsed to value-type"));
            e.addSuppressed(ee);
            throw e;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String from = context.<String>getOne("what").orElse("");
        if (from.equals("buff")) {
            Optional<String> next = args.nextIfPresent();
            if (next.isPresent()) {
                String arg = next.get();
                int idx = arg.lastIndexOf("_");
                int lastDot = arg.lastIndexOf(",");
                if (idx == -1 || lastDot > idx) {
                    List<String> list = new ArrayList<>();
                    for (ValueType value : ValueType.values()) {
                        list.add(arg + "_" + value.toString().toLowerCase());
                    }
                    return list;
                } else {
                    idx += 1;

                    String left = arg.substring(idx);
                    List<String> list = new ArrayList<>();
                    for (ValueType value : ValueType.values()) {
                        list.add(arg.substring(0, idx) + value.toString().toLowerCase());
                    }

                    for (String s : list) {
                        if (s.startsWith(left)) {
                            list.remove(s);
                            list.add(0, s);
                            break;
                        }
                    }
                    return list;
                }
            }

        } else if (from.equals("attribute")) {
            List<String> list = new ArrayList<>();
            for (ValueType value : ValueType.values()) {
                list.add(value.toString().toLowerCase());
            }
            args.nextIfPresent().ifPresent(left -> {
                for (String s : list) {
                    if (s.startsWith(left)) {
                        list.remove(s);
                        list.add(0, s);
                        break;
                    }
                }
            });
            return list;
        }
        return Collections.emptyList();
    }
}
