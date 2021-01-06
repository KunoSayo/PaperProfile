package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.IOptionNumber;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;
import io.github.euonmyoji.paperprofile.common.data.ValueType;
import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
import io.github.euonmyoji.paperprofile.manager.LanguageManager;
import io.github.euonmyoji.paperprofile.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.TypeTokens;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.euonmyoji.paperprofile.util.Util.STRING_LIST_TYPE;
import static org.spongepowered.api.text.Text.NEW_LINE;
import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public class PaperAttribute implements IPaperAttribute {
    public final String key;
    public final String name;
    public final List<String> lore;
    public final boolean systemAttribute;
    public final ValueType type;
    public final OptionNumber min;
    public final String def;
    public final int defn;
    public final OptionNumber max;
    public final String display;
    public final ItemStackSnapshot itemStackSnapshot;

    public PaperAttribute(String key, ValueType type, boolean systemAttribute) {
        this.key = key;
        this.type = type;
        this.systemAttribute = systemAttribute;

        this.name = key;
        this.lore = new ArrayList<>();
        this.min = new OptionNumber(0);
        this.def = type == ValueType.NUMBER ? "0" : "";
        this.defn = 0;
        this.max = new OptionNumber(Integer.MAX_VALUE);
        this.display = "[属性]{0}: %a_" + key + "_value%";
        this.itemStackSnapshot = getItemTemplate();
    }

    public PaperAttribute(String key, ValueType type, boolean systemAttribute, String buffKey) {
        this.key = key;
        this.type = type;
        this.systemAttribute = systemAttribute;

        this.name = key;
        this.lore = new ArrayList<>();
        this.min = new OptionNumber(0);
        this.def = type == ValueType.NUMBER ? "0" : "";
        this.defn = 0;
        this.max = new OptionNumber(Integer.MAX_VALUE);
        this.display = "[属性]{0}: %b_" + buffKey + "_" + key + "_value%";
        this.itemStackSnapshot = getItemTemplate();
    }

    public PaperAttribute(String key, CommentedConfigurationNode node) throws ObjectMappingException {
        if (node.isVirtual()) {
            throw new ObjectMappingException("cannot map object from virtual node.");
        }
        this.key = key;
        if (key.contains("_")) {
            //spam.
            throw new ObjectMappingException("The file must in question sea.", new IllegalArgumentException(key + " contains _"));
        }

        name = node.getNode("name").getString(key);
        lore = node.getNode("lore").getList(TypeTokens.STRING_TOKEN, new ArrayList<>());
        systemAttribute = node.getNode("permission").getString("system").equals("system");
        try {
            type = ValueType.valueOf(node.getNode("type").getString("string").toUpperCase());
        } catch (IllegalArgumentException e) {
            //bksw
            throw new ObjectMappingException(e);
        }

        min = new OptionNumber(node.getNode("min").getString("0"));
        if (type == ValueType.NUMBER) {
            String def = node.getNode("default").getString("0");
            if (def.isEmpty()) {
                def = "0";
            }
            this.def = def;
            try {
                defn = Integer.parseInt(def);
            } catch (NumberFormatException e) {
                throw new ObjectMappingException(e);
            }
        } else {
            def = node.getNode("default").getString("");
            defn = 0;
        }
        max = new OptionNumber(node.getNode("max").getString(Integer.MAX_VALUE + ""));

        display = node.getNode("display").getString("[属性]{0}: %a_" + key + "_value%");
        itemStackSnapshot = node.getNode("item").getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, getItemTemplate());
    }

    private ItemStackSnapshot getItemTemplate() {
        return ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.ITEM_LORE, lore.stream().map(Util::toText).collect(Collectors.toList()))
                .build().createSnapshot();
    }

    /**
     * save the value to the node
     *
     * @param node the node present the attributes node
     * @throws ObjectMappingException if some error in types
     */
    public void saveTo(CommentedConfigurationNode node) throws ObjectMappingException {
        node = node.getNode(key);

        node.getNode("name").setValue(name);
        node.getNode("lore").setValue(STRING_LIST_TYPE, lore);
        node.getNode("permission").setValue(systemAttribute ? "system" : "custom");
        node.getNode("type").setValue(type.toString().toLowerCase());
        min.saveTo(node.getNode("min"));
        node.getNode("default").setValue(def);
        max.saveTo(node.getNode("max"));
        node.getNode("display").setValue(display);
        node.getNode("item").setValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, itemStackSnapshot);
    }

    public Text getText() {
        return getText(null);
    }

    public Text getText(IPlayerConfig playerConfig) {
        Text.Builder builder = Text.builder();
        builder.append(Util.toText(name));
        builder.append(NEW_LINE);
        if (lore.isEmpty()) {
            builder.append(Text.of("类型:" + type.toString())).append(NEW_LINE);
            builder.append(Text.of("最小值:" + min.getInfo(playerConfig))).append(NEW_LINE);
            builder.append(Text.of("最大值:" + max.getInfo(playerConfig))).append(NEW_LINE);
            builder.append(Util.toText("默认值:" + def)).append(NEW_LINE);
            builder.append(of("显示格式: " + display)).append(NEW_LINE);
        } else {
            lore.stream().map(s -> PaperDataConfig.parse(playerConfig, s))
                    .map(Util::toText).forEach(text -> builder.append(text).append(NEW_LINE));
        }
        return Text.builder().append(Util.toText(name)).onHover(TextActions.showText(builder.build())).build();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public String getDefault() {
        return def;
    }

    @Override
    public int getDefaultN() {
        return defn;
    }

    @Override
    public String getDisplayFormat() {
        return display;
    }

    public Text format(IPlayerConfig playerConfig) {
        String parsed = PaperDataConfig.parse(playerConfig, display);
        return LanguageManager.format(parsed, getText(playerConfig));
    }

    @Override
    public IOptionNumber getMin() {
        return min;
    }

    @Override
    public IOptionNumber getMax() {
        return max;
    }
}
