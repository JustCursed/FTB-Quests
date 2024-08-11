package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.*;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonAddReward extends Button {
	private final Quest quest;

	public ButtonAddReward(Panel panel, Quest q) {
		super(panel, I18n.format("gui.add"), ThemeProperties.ADD_ICON.get());
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button) {
		GuiHelper.playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (RewardType type : RewardType.getRegistry()) {
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
				GuiHelper.playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, reward -> {
					NBTTagCompound extra = new NBTTagCompound();
					extra.setString("type", type.getTypeForNBT());
					new MessageCreateObject(reward, extra).sendToServer();
				});
			}));
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(theme, x, y, w, h);
		}
	}
}