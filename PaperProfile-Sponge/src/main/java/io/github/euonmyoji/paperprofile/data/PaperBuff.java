package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.config.IPlayerConfig;
import io.github.euonmyoji.paperprofile.common.data.IPaperAttribute;
import io.github.euonmyoji.paperprofile.common.data.IPaperBuff;
import io.github.euonmyoji.paperprofile.common.data.ValueType;
import io.github.euonmyoji.paperprofile.config.PaperDataConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.euonmyoji.paperprofile.util.Util.STRING_LIST_TYPE;
import static org.spongepowered.api.text.Text.NEW_LINE;

/**
 * @author yinyangshi
 */
public class PaperBuff implements IPaperBuff {
    public final String key;
    public final String name;
    public final List<String> lore;
    public final boolean systemBuff;
    public final Map<String, PaperAttribute> values;
    public final List<PaperTickOperator> ops = new ArrayList<>();
    public final String display;
    public final ItemStackSnapshot itemStackSnapshot;

    public PaperBuff(String key, Map<String, ValueType> values, boolean systemBuff) {
        this.key = key;
        this.values = new HashMap<>();
        values.forEach((s, valueType) -> this.values.put(s, new PaperAttribute(s, valueType, systemBuff, key)));
        this.systemBuff = systemBuff;

        this.name = key;
        this.lore = new ArrayList<>();

        this.display = "[状态]{0}";
        this.itemStackSnapshot = getItemTemplate();
    }


    public PaperBuff(String key, CommentedConfigurationNode node) throws ObjectMappingException {
        if (node.isVirtual()) {
            throw new ObjectMappingException("cannot map object from virtual node.");
        }
        this.key = key;
        name = node.getNode("name").getString(key);
        lore = node.getNode("lore").getList(TypeTokens.STRING_TOKEN, new ArrayList<>());
        systemBuff = node.getNode("permission").getString("system").equals("system");
        values = new HashMap<>();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.getNode("values").getChildrenMap().entrySet()) {
            String vkey = entry.getKey().toString();
            values.put(vkey, new PaperAttribute(vkey, entry.getValue()));
        }

        for (String v : node.getNode("gt_op").getList(TypeTokens.STRING_TOKEN, new ArrayList<>())) {
            ops.add(new PaperTickOperator(v));
        }
        this.display = node.getNode("display").getString("状态{0}");
        itemStackSnapshot = node.getNode("item").getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, getItemTemplate());
    }

    private ItemStackSnapshot getItemTemplate() {
        return ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.ITEM_LORE, lore.stream().map(Util::toText).collect(Collectors.toList()))
                .build().createSnapshot();
    }

    public void saveTo(CommentedConfigurationNode node) throws ObjectMappingException {
        node = node.getNode(key);

        node.getNode("name").setValue(name);
        node.getNode("lore").setValue(STRING_LIST_TYPE, lore);
        node.getNode("permission").setValue(this.systemBuff ? "system" : "custom");
        CommentedConfigurationNode valueNode = node.getNode("values");
        for (Map.Entry<String, PaperAttribute> entry : this.values.entrySet()) {
            entry.getValue().saveTo(valueNode);
        }
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
            this.values.forEach((s, paperAttribute) -> builder.append(paperAttribute.format(playerConfig)).append(NEW_LINE));
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
    public Map<String, ? extends IPaperAttribute> getValues() {
        return values;
    }

    @Override
    public String getDisplayFormat() {
        return display;
    }
}
