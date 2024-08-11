package com.feed_the_beast.ftbquests.integration.projecte;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.ISingleLongValueTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class EMCTask extends Task implements ISingleLongValueTask {
	public long value = 8192L;

	public EMCTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return ProjectEIntegration.EMC_TASK;
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public String getMaxProgressString() {
		return String.format("%,d", value);
	}

	@Override
	public void writeData(NBTTagCompound nbt) {
		super.writeData(nbt);
		nbt.setLong("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt) {
		super.readData(nbt);
		value = nbt.getLong("value");
	}

	@Override
	public void writeNetData(DataOut data) {
		super.writeNetData(data);
		data.writeVarLong(value);
	}

	@Override
	public void readNetData(DataIn data) {
		super.readNetData(data);
		value = data.readVarLong();
	}

	@Override
	public ConfigLong getDefaultValue() {
		return new ConfigLong(value, 1L, Long.MAX_VALUE);
	}

	@Override
	public void setValue(long v) {
		value = v;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addLong("value", () -> value, v -> value = v, 8192L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.task.ftbquests.emc"));
	}

	@Override
	public String getAltTitle() {
		return I18n.format("ftbquests.task.ftbquests.emc") + ": " + TextFormatting.AQUA + getMaxProgressString();
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public TaskData createData(QuestData data) {
		return new Data(this, data);
	}

	public static class Data extends TaskData<EMCTask> {
		private Data(EMCTask task, QuestData data) {
			super(task, data);
		}

		@Override
		public String getProgressString() {
			return String.format("%,d", progress);
		}

		@Override
		public void submitTask(EntityPlayerMP player, ItemStack item) {
			if (isComplete()) {
				return;
			}

			IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(player.getUniqueID());

			long emc = knowledge.getEmc();
			long add = Math.min(emc, task.value - progress);

			if (add > 0L) {
				knowledge.setEmc(emc - add);
				knowledge.sync(player);
				addProgress(add);
			}
		}
	}
}