package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class AdvancementTask extends Task {
	public String advancement = "";
	public String criterion = "";

	public AdvancementTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return FTBQuestsTasks.ADVANCEMENT;
	}

	@Override
	public void writeData(NBTTagCompound nbt) {
		super.writeData(nbt);
		nbt.setString("advancement", advancement);
		nbt.setString("criterion", criterion);
	}

	@Override
	public void readData(NBTTagCompound nbt) {
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(DataOut data) {
		super.writeNetData(data);
		data.writeString(advancement);
		data.writeString(criterion);
	}

	@Override
	public void readNetData(DataIn data) {
		super.readNetData(data);
		advancement = data.readString();
		criterion = data.readString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("advancement", () -> advancement, v -> advancement = v, "").setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.advancement"));
		config.addString("criterion", () -> criterion, v -> criterion = v, "");
	}

	@Override
	public String getAltTitle() {
		Advancement a = Minecraft.getMinecraft().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null) {
			return I18n.format("ftbquests.task.ftbquests.advancement") + ": " + TextFormatting.YELLOW + a.getDisplay().getTitle().getFormattedText();
		}

		return super.getAltTitle();
	}

	@Override
	public Icon getAltIcon() {
		Advancement a = Minecraft.getMinecraft().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));
		return a == null || a.getDisplay() == null ? super.getAltIcon() : ItemIcon.getItemIcon(a.getDisplay().getIcon());
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 5;
	}

	@Override
	public TaskData createData(QuestData data) {
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<AdvancementTask> {
		private Data(AdvancementTask task, QuestData data) {
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player) {
			if (task.advancement.isEmpty()) {
				return false;
			}

			Advancement a = player.server.getAdvancementManager().getAdvancement(new ResourceLocation(task.advancement));

			if (a == null) {
				return false;
			}

			AdvancementProgress progress = player.getAdvancements().getProgress(a);

			if (task.criterion.isEmpty()) {
				return progress.isDone();
			} else {
				CriterionProgress criterionProgress = progress.getCriterionProgress(task.criterion);
				return criterionProgress != null && criterionProgress.isObtained();
			}
		}
	}
}