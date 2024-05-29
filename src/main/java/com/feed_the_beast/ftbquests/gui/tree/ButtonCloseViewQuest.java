package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonCloseViewQuest extends SimpleTextButton {
    public ButtonCloseViewQuest(PanelViewQuest parent) {
        super(parent, I18n.format("gui.close"), ThemeProperties.CLOSE_ICON.get(parent.quest));
    }

    @Override
    public void onClicked(MouseButton button) {
        GuiHelper.playClickSound();
        ((GuiQuestTree) getGui()).closeQuest();
    }

    @Override
    public void drawBackground(Theme theme, int x, int y, int w, int h) {
    }
}