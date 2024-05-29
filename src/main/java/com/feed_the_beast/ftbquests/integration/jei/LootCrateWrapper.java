package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateWrapper implements IRecipeWrapper, ITooltipCallback<ItemStack> {
    public final LootCrate crate;
    public final String name;
    public final ItemStack itemStack;
    public final List<ItemStack> items;
    public final List<WeightedReward> rewards;
    public final List<List<ItemStack>> itemLists;

    public LootCrateWrapper(LootCrate c) {
        crate = c;
        name = crate.table.getTitle();
        itemStack = crate.createStack();
        items = new ArrayList<>(c.table.rewards.size());

        rewards = new ArrayList<>(c.table.rewards);
        rewards.sort(null);

        for (WeightedReward reward : rewards) {
            Object object = reward.reward.getIngredient();
            ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

            if (!stack.isEmpty()) {
                items.add(stack.copy());
            } else if (reward.reward.getIcon() instanceof ItemIcon) {
                stack = ((ItemIcon) reward.reward.getIcon()).getStack().copy();
                stack.setStackDisplayName(reward.reward.getTitle());
                items.add(stack);
            } else {
                stack = new ItemStack(Items.PAINTING);
                stack.setStackDisplayName(reward.reward.getTitle());
                stack.setTagInfo("icon", new NBTTagString(reward.reward.getIcon().toString()));
                items.add(stack);
            }
        }

        if (items.size() <= LootCrateCategory.ITEMS) {
            itemLists = new ArrayList<>(items.size());

            for (ItemStack stack : items) {
                itemLists.add(Collections.singletonList(stack));
            }
        } else {
            itemLists = new ArrayList<>(LootCrateCategory.ITEMS);

            for (int i = 0; i < LootCrateCategory.ITEMS; i++) {
                itemLists.add(new ArrayList<>());
            }

            for (int i = 0; i < items.size(); i++) {
                itemLists.get(i % LootCrateCategory.ITEMS).add(items.get(i));
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, itemStack);
        ingredients.setOutputLists(VanillaTypes.ITEM, itemLists);
    }

    private String chance(String type, int w, int t) {
        String s = I18n.format("ftbquests.loot.entitytype." + type) + ": " + WeightedReward.chanceString(w, t);

        if (w > 0) {
            s += " (1 in " + StringUtils.formatDouble00(1D / ((double) w / (double) t)) + ")";
        }

        return s;
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GuiHelper.drawItem(itemStack, 0, 0, 2, 2, true);
        mc.fontRenderer.drawString(TextFormatting.UNDERLINE + crate.table.getUnformattedTitle(), 36, 0, 0xFF222222);

        int total = ClientQuestFile.INSTANCE.lootCrateNoDrop.passive;

        for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
            if (table.lootCrate != null) {
                total += table.lootCrate.drops.passive;
            }
        }

        mc.fontRenderer.drawString(chance("passive", crate.drops.passive, total), 36, 10, 0xFF222222);

        total = ClientQuestFile.INSTANCE.lootCrateNoDrop.monster;

        for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
            if (table.lootCrate != null) {
                total += table.lootCrate.drops.monster;
            }
        }

        mc.fontRenderer.drawString(chance("monster", crate.drops.monster, total), 36, 19, 0xFF222222);

        total = ClientQuestFile.INSTANCE.lootCrateNoDrop.boss;

        for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
            if (table.lootCrate != null) {
                total += table.lootCrate.drops.boss;
            }
        }

        mc.fontRenderer.drawString(chance("boss", crate.drops.boss, total), 36, 28, 0xFF222222);
    }

    @Override
    public void onTooltip(int slot, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (slot > 0 && slot - 1 < items.size()) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) == ingredient) {
                    tooltip.add(TextFormatting.GRAY + I18n.format("jei.ftbquests.lootcrates.chance", TextFormatting.GOLD + WeightedReward.chanceString(rewards.get(i).weight, crate.table.getTotalWeight(true))));
                    return;
                }
            }
        }
    }
}