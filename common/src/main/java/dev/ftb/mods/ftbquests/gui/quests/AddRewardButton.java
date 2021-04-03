package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.widget.Button;
import dev.ftb.mods.ftbguilibrary.widget.ContextMenuItem;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbquests.net.MessageCreateObject;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class AddRewardButton extends Button {
	private final Quest quest;

	public AddRewardButton(Panel panel, Quest q) {
		super(panel, new TranslatableComponent("gui.add"), ThemeProperties.ADD_ICON.get());
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (RewardType type : RewardTypes.TYPES.values()) {
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
				playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, reward -> {
					CompoundTag extra = new CompoundTag();
					extra.putString("type", type.getTypeForNBT());
					new MessageCreateObject(reward, extra).sendToServer();
				});
			}));
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(matrixStack, theme, x, y, w, h);
		}
	}
}