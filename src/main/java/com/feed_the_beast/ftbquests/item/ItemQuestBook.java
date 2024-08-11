package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemQuestBook extends Item {
	public ItemQuestBook() {
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			openGui(player);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	private void openGui(EntityPlayer player) {
		ClientQuestFile.INSTANCE.openQuestGui(player);
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		return stack.copy();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.disableGui && !ClientQuestFile.INSTANCE.editingMode) {
			tooltip.add(TextFormatting.RED + I18n.format("item.ftbquests.book.disabled"));
		} else {
			tooltip.add(I18n.format("item.ftbquests.book.tooltip"));
		}
	}
}