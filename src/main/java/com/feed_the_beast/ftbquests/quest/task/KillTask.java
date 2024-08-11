package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class KillTask extends Task {
	public static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft:zombie");
	public ResourceLocation entity = ZOMBIE;
	public long value = 100L;

	public static StatBase get(String id) {
		for (StatBase base : StatList.BASIC_STATS) {
			if (base.statId.equals(id)) {
				return base;
			}
		}

		return StatList.MOB_KILLS;
	}

	public KillTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return FTBQuestsTasks.KILL;
	}

	@Override
	public long getMaxProgress() {
		return value;
	}

	@Override
	public void writeData(NBTTagCompound nbt) {
		super.writeData(nbt);
		nbt.setString("entity", entity.toString());
		nbt.setLong("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt) {
		super.readData(nbt);
		entity = new ResourceLocation(nbt.getString("entity"));
		value = nbt.getInteger("value");
	}

	@Override
	public void writeNetData(DataOut data) {
		super.writeNetData(data);
		data.writeString(entity.toString());
		data.writeVarLong(value);
	}

	@Override
	public void readNetData(DataIn data) {
		super.readNetData(data);
		entity = new ResourceLocation(data.readString());
		value = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		List<ResourceLocation> ids = new ArrayList<>();

		for (EntityEntry entry : ForgeRegistries.ENTITIES) {
			if (EntityLivingBase.class.isAssignableFrom(entry.getEntityClass())) {
				ids.add(entry.getRegistryName());
			}
		}

		config.addEnum("entity", () -> entity, v -> entity = v, NameMap.createWithTranslation(ZOMBIE, (sender, s) -> "entity." + EntityList.getTranslationName(s) + ".name", ids.toArray(new ResourceLocation[0])));
		config.addLong("value", () -> value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
	}

	@Override
	public String getAltTitle() {
		return I18n.format("ftbquests.task.ftbquests.kill.title", getMaxProgressString(), I18n.format("entity." + EntityList.getTranslationName(entity) + ".name"));
	}

	@Override
	public Icon getAltIcon() {
		if (EntityList.ENTITY_EGGS.containsKey(entity)) {
			ItemStack stack = new ItemStack(Items.SPAWN_EGG);
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("id", entity.toString());
			stack.setTagInfo("EntityTag", nbt);
			return ItemIcon.getItemIcon(stack);
		}

		return super.getAltIcon();
	}

	@Override
	public TaskData createData(QuestData data) {
		return new Data(this, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick) {
	}

	public static class Data extends TaskData<KillTask> {
		private Data(KillTask task, QuestData data) {
			super(task, data);
		}

		public void kill(EntityLivingBase entity) {
			if (!isComplete() && task.entity.equals(EntityList.getKey(entity))) {
				addProgress(1L);
			}
		}
	}
}