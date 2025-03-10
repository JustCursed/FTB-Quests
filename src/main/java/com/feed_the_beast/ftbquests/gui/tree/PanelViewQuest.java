package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.*;
import com.feed_the_beast.ftblib.lib.gui.misc.CompactGridLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.theme.QuestTheme;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PanelViewQuest extends Panel {
	public final GuiQuestTree gui;
	public Quest quest = null;
	public boolean hidePanel = false;
	private String title = "";
	private Icon icon = Icon.EMPTY;
	public Button buttonClose;
	public Button buttonOnScreen;
	public Button buttonOpenDependencies;
	public Button buttonOpenDependants;
	public BlankPanel panelContent;
	public BlankPanel panelTasks;
	public BlankPanel panelRewards;
	public BlankPanel panelText;

	public PanelViewQuest(GuiQuestTree g) {
		super(g);
		gui = g;
		setPosAndSize(-1, -1, 0, 0);
		setOnlyRenderWidgetsInside(true);
		setOnlyInteractWithWidgetsInside(true);
	}

	@Override
	public void addWidgets() {
		setPosAndSize(-1, -1, 1, 1);

		if (quest == null || hidePanel) {
			return;
		}

		QuestObjectBase prev = QuestTheme.currentObject;
		QuestTheme.currentObject = quest;

		setScrollX(0);
		setScrollY(0);

		title = quest.getTitle();
		icon = quest.getIcon();

		int w = Math.max(200, gui.getTheme().getStringWidth(title) + 50);

		add(panelContent = new BlankPanel(this, "ContentPanel"));
		panelContent.add(panelTasks = new BlankPanel(panelContent, "TasksPanel"));
		panelContent.add(panelRewards = new BlankPanel(panelContent, "RewardsPanel"));
		panelContent.add(panelText = new BlankPanel(panelContent, "TextPanel"));

		boolean canEdit = gui.file.canEdit();
		int bsize = 18;

		for (Task task : quest.tasks) {
			ButtonTask b = new ButtonTask(panelTasks, task);
			panelTasks.add(b);
			b.setSize(bsize, bsize);
		}

		if (!canEdit && panelTasks.widgets.isEmpty()) {
			TextFieldDisabledButton noTasks = new TextFieldDisabledButton(panelTasks, I18n.format("ftbquests.gui.no_tasks"));
			noTasks.setSize(noTasks.width + 8, bsize);
			noTasks.setColor(ThemeProperties.DISABLED_TEXT_COLOR.get(quest));
			panelTasks.add(noTasks);
		}

		for (Reward reward : quest.rewards) {
			if (canEdit || reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE) {
				ButtonReward b = new ButtonReward(panelRewards, reward);
				panelRewards.add(b);
				b.setSize(bsize, bsize);
			}
		}

		if (!canEdit && panelRewards.widgets.isEmpty()) {
			TextFieldDisabledButton noRewards = new TextFieldDisabledButton(panelRewards, I18n.format("ftbquests.gui.no_rewards"));
			noRewards.setSize(noRewards.width + 8, bsize);
			noRewards.setColor(ThemeProperties.DISABLED_TEXT_COLOR.get(quest));
			panelRewards.add(noRewards);
		}

		if (gui.file.canEdit()) {
			panelTasks.add(new ButtonAddTask(panelTasks, quest));
			panelRewards.add(new ButtonAddReward(panelRewards, quest));
		}

		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(gui.selectedChapter);

		int ww = 0;

		for (Widget widget : panelTasks.widgets) {
			ww = Math.max(ww, widget.width);
		}

		for (Widget widget : panelRewards.widgets) {
			ww = Math.max(ww, widget.width);
		}

		ww = MathHelper.clamp(ww, 70, 140);
		w = Math.max(w, ww * 2 + 10);
		w = Math.max(w, quest.minWidth);

		if (ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
			w = gui.width - 1;
		}

		if (w % 2 == 0) {
			w++;
		}

		setWidth(w);
		panelContent.setPosAndSize(0, 16, w, 0);
		int w2 = panelContent.width / 2;

		add(buttonClose = new ButtonCloseViewQuest(this));
		buttonClose.setPosAndSize(w - 14, 2, 12, 12);

		add(buttonOnScreen = new ButtonPinViewQuest(this));
		buttonOnScreen.setPosAndSize(w - 26, 2, 12, 12);

		if (quest.dependencies.isEmpty()) {
			add(buttonOpenDependencies = new SimpleButton(this, I18n.format("ftbquests.gui.no_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependencies = new SimpleButton(this, I18n.format("ftbquests.gui.view_dependencies"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_left.png").withTint(borderColor), (widget, button) -> showList(quest.dependencies)));
		}

		if (quest.getDependants().isEmpty()) {
			add(buttonOpenDependants = new SimpleButton(this, I18n.format("ftbquests.gui.no_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(borderColor), (widget, button) -> {
			}));
		} else {
			add(buttonOpenDependants = new SimpleButton(this, I18n.format("ftbquests.gui.view_dependants"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/arrow_right.png").withTint(borderColor), (widget, button) -> showList(quest.getDependants())));
		}

		buttonOpenDependencies.setPosAndSize(0, 17, 13, 13);
		buttonOpenDependants.setPosAndSize(w - 13, 17, 13, 13);

		TextField textFieldTasks = new TextField(panelContent) {
			@Override
			public TextField resize(Theme theme) {
				return this;
			}
		};

		textFieldTasks.setPosAndSize(2, 2, w2 - 3, 13);
		textFieldTasks.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldTasks.setText(I18n.format("ftbquests.tasks"));
		textFieldTasks.setColor(ThemeProperties.TASKS_TEXT_COLOR.get(quest));
		panelContent.add(textFieldTasks);

		TextField textFieldRewards = new TextField(panelContent) {
			@Override
			public TextField resize(Theme theme) {
				return this;
			}
		};

		textFieldRewards.setPosAndSize(w2 + 2, 2, w2 - 3, 13);
		textFieldRewards.addFlags(Theme.CENTERED | Theme.CENTERED_V);
		textFieldRewards.setText(I18n.format("ftbquests.rewards"));
		textFieldRewards.setColor(ThemeProperties.REWARDS_TEXT_COLOR.get(quest));
		panelContent.add(textFieldRewards);

		panelTasks.setPosAndSize(2, 16, w2 - 3, 0);
		panelRewards.setPosAndSize(w2 + 2, 16, w2 - 3, 0);

		int at = panelTasks.align(new CompactGridLayout(bsize + 2));
		int ar = panelRewards.align(new CompactGridLayout(bsize + 2));

		int h = Math.max(at, ar);
		panelTasks.setHeight(h);
		panelRewards.setHeight(h);

		int tox = (panelTasks.width - panelTasks.getContentWidth()) / 2;
		int rox = (panelRewards.width - panelRewards.getContentWidth()) / 2;
		int toy = (panelTasks.height - panelTasks.getContentHeight()) / 2;
		int roy = (panelRewards.height - panelRewards.getContentHeight()) / 2;

		for (Widget widget : panelTasks.widgets) {
			widget.setX(widget.posX + tox);
			widget.setY(widget.posY + toy);
		}

		for (Widget widget : panelRewards.widgets) {
			widget.setX(widget.posX + rox);
			widget.setY(widget.posY + roy);
		}

		panelText.setPosAndSize(3, 16 + h + 12, panelContent.width - 6, 0);

		String desc = quest.getSubtitle();

		if (!desc.isEmpty()) {
			panelText.add(new TextField(panelText).addFlags(Theme.CENTERED).setMaxWidth(panelText.width).setSpacing(9).setText(TextFormatting.ITALIC + TextFormatting.GRAY.toString() + desc));
		}

		boolean showText = !quest.hideTextUntilComplete || gui.file.self != null && quest.isComplete(gui.file.self);

		if (showText && quest.getDescription().length > 0) {
			if (!desc.isEmpty()) {
				panelText.add(new WidgetVerticalSpace(panelText, 7));
			}

			panelText.add(new TextField(panelText).setMaxWidth(panelText.width).setSpacing(9).setText(StringUtils.addFormatting(StringJoiner.with('\n').joinStrings(quest.getDescription()))));
		}

		if (showText && !quest.guidePage.isEmpty()) {
			if (!desc.isEmpty()) {
				panelText.add(new WidgetVerticalSpace(panelText, 7));
			}

			panelText.add(new ButtonOpenInGuide(panelText, quest));
		}

		if (panelText.widgets.isEmpty()) {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, h + 40));
			panelText.setHeight(0);
			setHeight(Math.min(panelContent.getContentHeight(), parent.height - 10));
		} else {
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(w2, 0, 1, 16 + h + 6));
			panelContent.add(new ColorWidget(panelContent, borderColor, null).setPosAndSize(1, 16 + h + 6, panelContent.width - 2, 1));
			panelText.setHeight(panelText.align(new WidgetLayout.Vertical(0, 0, 1)));
			setHeight(Math.min(panelContent.getContentHeight() + 20, parent.height - 10));
		}

		if (ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
			height = gui.height;
		}

		setPos((parent.width - width) / 2, (parent.height - height) / 2);
		panelContent.setHeight(height - 17);

		/* Put this somewhere
		boolean addedText = false;

		for (QuestObject dependency : selectedQuest.dependencies)
		{
			if (!dependency.invalid)
			{
				if (!addedText)
				{
					addedText = true;
					add(new WidgetVerticalSpace(this, 2));
					add(new TextField(this).setText(TextFormatting.AQUA + I18n.format("ftbquests.gui.requires") + ":"));
				}

				ITextComponent component = dependency.getDisplayName().createCopy();
				component.getStyle().setColor(TextFormatting.GRAY);
				component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, dependency.toString()));
				component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gui.open")));
				add(new TextField(this).setText(component));
			}
		}

		addedText = false;

		for (Chapter chapter : treeGui.file.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.hasDependency(selectedQuest))
				{
					if (!addedText)
					{
						addedText = true;
						add(new WidgetVerticalSpace(this, 2));
						add(new TextField(this, TextFormatting.YELLOW + I18n.format("ftbquests.gui.required_by") + ":"));
					}

					ITextComponent component = quest.getDisplayName().createCopy();
					component.getStyle().setColor(TextFormatting.GRAY);
					component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, quest.toString()));
					component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gui.open")));
					add(new TextField(this).setText(component));
				}
			}
		}
		*/

		QuestTheme.currentObject = prev;
	}

	private void showList(Collection<QuestObject> c) {
		int hidden = 0;
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestObject object : c) {
			if (gui.file.canEdit() || object.isVisible(gui.file.self)) {
				contextMenu.add(new ContextMenuItem(object.getTitle(), Icon.EMPTY, () -> gui.open(object, true)));
			} else {
				hidden++;
			}
		}

		if (hidden > 0) {
			if (hidden == c.size()) {
				contextMenu.add(new ContextMenuItem(hidden + " hidden quests", Icon.EMPTY, () -> {
				}).setEnabled(false));
			} else {
				contextMenu.add(new ContextMenuItem("+ " + hidden + " hidden quests", Icon.EMPTY, () -> {
				}).setEnabled(false));
			}
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void alignWidgets() {
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h) {
		if (quest != null && !hidePanel) {
			QuestObjectBase prev = QuestTheme.currentObject;
			QuestTheme.currentObject = quest;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			super.draw(theme, x, y, w, h);
			GlStateManager.popMatrix();
			QuestTheme.currentObject = prev;
		}
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h) {
		Color4I borderColor = ThemeProperties.QUEST_VIEW_BORDER.get();
		Color4I.DARK_GRAY.withAlpha(120).draw(gui.getX(), gui.getY(), gui.width, gui.height);
		Icon background = ThemeProperties.QUEST_VIEW_BACKGROUND.get();
		background.draw(x, y, w, h);
		theme.drawString(title, x + w / 2, y + 4, ThemeProperties.QUEST_VIEW_TITLE.get(), Theme.CENTERED);
		icon.draw(x + 2, y + 2, 12, 12);
		borderColor.draw(x + 1, y + 15, w - 2, 1);
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		return super.mousePressed(button) || isMouseOver();
	}
}