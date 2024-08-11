package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.*;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.latmod.mods.itemfilters.api.IItemFilter;
import com.latmod.mods.itemfilters.api.ItemFiltersAPI;
import com.latmod.mods.itemfilters.filters.OreDictionaryFilter;
import com.latmod.mods.itemfilters.item.ItemFilter;
import com.latmod.mods.itemfilters.item.ItemFiltersItems;
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
public class ButtonTask extends Button {
	public final GuiQuestTree treeGui;
	public Task task;

	public ButtonTask(Panel panel, Task t) {
		super(panel, t.getTitle(), GuiIcons.ACCEPT);
		treeGui = (GuiQuestTree) panel.getGui();
		task = t;
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
	public void onClicked(MouseButton button) {
		if (button.isLeft()) {
			task.onButtonClicked(!(task.invalid || treeGui.file.self == null || !task.quest.canStartTasks(treeGui.file.self) || task.isComplete(treeGui.file.self)));
		} else if (button.isRight() && treeGui.file.canEdit()) {
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			if (task instanceof ItemTask) {
				ItemTask i = (ItemTask) task;

				if (i.items.size() == 1 && !(i.items.get(0).getItem() instanceof ItemFilter)) {
					List<String> oreNames = new ArrayList<>(InvUtils.getOreNames(null, i.items.get(0)));

					if (!oreNames.isEmpty()) {
						contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.task.ftbquests.item.convert_oredict"), ThemeProperties.RELOAD_ICON.get(), () -> {
							ItemStack oreFilter = new ItemStack(ItemFiltersItems.FILTER);
							IItemFilter filter = ItemFiltersAPI.getFilter(oreFilter);

							if (filter instanceof ItemFilter.ItemFilterData) {
								((ItemFilter.ItemFilterData) filter).filter = new OreDictionaryFilter();

								if (oreNames.size() == 1) {
									((OreDictionaryFilter) ((ItemFilter.ItemFilterData) filter).filter).setValue(oreNames.get(0));

									i.items.clear();
									i.items.add(oreFilter.setStackDisplayName("Any " + oreNames.get(0)));

									new MessageEditObject(i).sendToServer();
								} else {
									new GuiButtonListBase() {
										@Override
										public void addButtons(Panel panel) {
											for (String s : oreNames) {
												panel.add(new SimpleTextButton(panel, s, Icon.EMPTY) {
													@Override
													public void onClicked(MouseButton button) {
														treeGui.openGui();
														((OreDictionaryFilter) ((ItemFilter.ItemFilterData) filter).filter).setValue(s);

														i.items.clear();
														i.items.add(oreFilter.setStackDisplayName("Any " + s));

														new MessageEditObject(i).sendToServer();
													}
												});
											}
										}
									}.openGui();
								}
							}
						}));

						contextMenu.add(ContextMenuItem.SEPARATOR);
					}
				}
			}

			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), task);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse() {
		return task.getIngredient();
	}

	@Override
	public void addMouseOverText(List<String> list) {
		if (isShiftKeyDown() && isCtrlKeyDown()) {
			list.add(TextFormatting.DARK_GRAY + task.toString());
		}

		if (task.addTitleInMouseOverText()) {
			list.add(getTitle());
		}

		TaskData data;

		if (treeGui.file.self != null && task.quest.canStartTasks(treeGui.file.self)) {
			data = treeGui.file.self.getTaskData(task);
			long maxp = task.getMaxProgress();

			if (maxp > 1L) {
				if (task.hideProgressNumbers()) {
					list.add(TextFormatting.DARK_GREEN + "[" + data.getRelativeProgress() + "%]");
				} else {
					String max = isShiftKeyDown() ? Long.toUnsignedString(maxp) : task.getMaxProgressString();
					String prog = isShiftKeyDown() ? Long.toUnsignedString(data.progress) : data.getProgressString();

					if (maxp < 100L) {
						list.add(TextFormatting.DARK_GREEN + (data.progress > maxp ? max : prog) + " / " + max);
					} else {
						list.add(TextFormatting.DARK_GREEN + (data.progress > maxp ? max : prog) + " / " + max + TextFormatting.DARK_GRAY + " [" + data.getRelativeProgress() + "%]");
					}

				}
			}
		} else {
			data = null;
			//list.add(TextFormatting.DARK_GRAY + "[0%]");
		}

		task.addMouseOverText(list, data);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(theme, x, y, w, h);
		}
	}

	@Override
	public void drawIcon(Theme theme, int x, int y, int w, int h) {
		task.drawGUI(treeGui.file.self == null ? null : treeGui.file.self.getTaskData(task), x, y, w, h);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
		drawBackground(theme, x, y, w, h);
		drawIcon(theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		if (treeGui.file.self == null || treeGui.contextMenu != null) {
			return;
		}

		if (task.isComplete(treeGui.file.self)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			ThemeProperties.CHECK_ICON.get().draw(x + w - 9, y + 1, 8, 8);
			GlStateManager.popMatrix();
		} else {
			String s = task.getButtonText();

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