package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.data.ValueType;
import io.github.euonmyoji.paperprofile.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
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
public class PaperBuff {
    public final String key;
    public final String name;
    public final List<Text> lore;
    public final boolean systemBuff;
    public final Map<String, PaperAttribute> values;
    public final List<PaperTickOperator> ops = new ArrayList<>();
    public final ItemStackSnapshot itemStackSnapshot;

    public PaperBuff(String key, Map<String, ValueType> values, boolean systemBuff) {
        this.key = key;
        this.values = new HashMap<>();
        values.forEach((s, valueType) -> this.values.put(s, new PaperAttribute(s, valueType, systemBuff)));
        this.systemBuff = systemBuff;

        this.name = key;
        this.lore = new ArrayList<>();

        this.itemStackSnapshot = getItemTemplate();
    }


    public PaperBuff(String key, CommentedConfigurationNode node) throws ObjectMappingException {
        this.key = key;
        name = node.getNode("name").getString(key);
        lore = node.getNode("lore").getList(TypeTokens.STRING_TOKEN, new ArrayList<>())
                .stream().map(Util::toText)
                .collect(Collectors.toList());
        systemBuff = node.getNode("permission").getString("system").equals("system");
        values = new HashMap<>();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.getNode("values").getChildrenMap().entrySet()) {
            String vkey = entry.getKey().toString();
            values.put(vkey, new PaperAttribute(vkey, entry.getValue()));
        }

        for (String v : node.getNode("gt_op").getList(TypeTokens.STRING_TOKEN, new ArrayList<>())) {
            ops.add(new PaperTickOperator(v));
        }

        itemStackSnapshot = node.getNode("item").getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, getItemTemplate());
    }

    private ItemStackSnapshot getItemTemplate() {
        return ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.ITEM_LORE, lore)
                .build().createSnapshot();
    }

    public void saveTo(CommentedConfigurationNode node) throws ObjectMappingException {
        node = node.getNode(key);

        node.getNode("name").setValue(name);
        node.getNode("lore").setValue(STRING_LIST_TYPE, lore.stream().map(Util::toStr).collect(Collectors.toList()));
        node.getNode("permission").setValue(this.systemBuff ? "system" : "custom");
        CommentedConfigurationNode valueNode = node.getNode("values");
        for (Map.Entry<String, PaperAttribute> entry : this.values.entrySet()) {
            entry.getValue().saveTo(valueNode.getNode(entry.getKey()));
        }
        node.getNode("item").setValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, itemStackSnapshot);
    }

    public Text getText() {
        Text.Builder builder = Text.builder();
        builder.append(Text.of(name));
        builder.append(NEW_LINE);
        for (Text text : lore) {
            builder.append(text).append(NEW_LINE);
        }
        this.values.forEach((s, paperAttribute) -> {
            builder.append(paperAttribute.getText());
        });
        return builder.build();
    }
}
