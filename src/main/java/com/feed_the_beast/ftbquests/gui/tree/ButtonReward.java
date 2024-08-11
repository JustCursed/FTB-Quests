package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.*;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends Button {
	public final GuiQuestTree treeGui;
	public final Reward reward;

	public ButtonReward(Panel panel, Reward r) {
		super(panel, r.getTitle(), r.getIcon());
		treeGui = (GuiQuestTree) panel.getGui();
		reward = r;
		setSize(18, 18);
	}

	@Override
	public void addMouseOverText(List<String> list) {
		if (isShiftKeyDown() && isCtrlKeyDown()) {
			list.add(TextFormatting.DARK_GRAY + reward.toString());
		}

		if (reward.isTeamReward()) {
			if (reward.addTitleInMouseOverText()) {
				list.add(getTitle());
			}

			Object object = getIngredientUnderMouse();

			if (object instanceof WrappedIngredient && ((WrappedIngredient) object).tooltip) {
				Object ingredient = WrappedIngredient.unwrap(object);

				if (ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty()) {
					GuiHelper.addStackTooltip((ItemStack) ingredient, list);
				}
			}

			list.add("");
			reward.addMouseOverText(list);
			list.add(TextFormatting.BLUE.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.reward.team_reward"));
		} else {
			if (reward.addTitleInMouseOverText()) {
				list.add(getTitle());
			}

			reward.addMouseOverText(list);
		}
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (button.isRight() || getWidgetType() != WidgetType.DISABLED) {
				onClicked(button);
			}

			return true;
		}

		return false;
	}

	@Override
	public WidgetType getWidgetType() {
		if (!ClientQuestFile.existsWithTeam() || !reward.quest.isComplete(ClientQuestFile.INSTANCE.self)) {
			return WidgetType.DISABLED;
		}

		return super.getWidgetType();
	}

	@Override
	public void onClicked(MouseButton button) {
		if (button.isLeft()) {
			if (ClientQuestFile.existsWithTeam()) {
				reward.onButtonClicked(reward.quest.isComplete(ClientQuestFile.INSTANCE.self) && !ClientQuestFile.INSTANCE.self.isRewardClaimedSelf(reward));
			}
		} else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit()) {
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), reward);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse() {
		return reward.getIngredient();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(theme, x, y, w, h);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
		drawBackground(theme, x, y, w, h);
		drawIcon(theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		if (treeGui.file.self == null || treeGui.contextMenu != null) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 0F, 500F);
		boolean completed = false;

		if (!ClientQuestFile.existsWithTeam()) {
			GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
		} else if (treeGui.file.self.isRewardClaimedSelf(reward)) {
			ThemeProperties.CHECK_ICON.get().draw(x + w - 9, y + 1, 8, 8);
			completed = true;
		} else if (reward.quest.isComplete(treeGui.file.self)) {
			ThemeProperties.ALERT_ICON.get().draw(x + w - 9, y + 1, 8, 8);
		}

		GlStateManager.popMatrix();

		if (!completed) {
			String s = reward.getButtonText();

			if (!s.isEmpty()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + 19F - theme.getStringWidth(s) / 2F, y + 15F, 500F);
				GlStateManager.scale(0.5F, 0.5F, 1F);
				theme.drawString(s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				GlStateManager.popMatrix();
			}
		}
	}
}