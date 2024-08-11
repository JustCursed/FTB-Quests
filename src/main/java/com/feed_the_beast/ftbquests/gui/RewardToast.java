package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.misc.SimpleToast;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class RewardToast extends SimpleToast {
	private final String text;
	private final Icon icon;

	public RewardToast(String t, Icon i) {
		text = t;
		icon = i;
	}

	@Override
	public String getTitle() {
		return I18n.format("ftbquests.reward.collected");
	}

	@Override
	public String getSubtitle() {
		return text;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
}