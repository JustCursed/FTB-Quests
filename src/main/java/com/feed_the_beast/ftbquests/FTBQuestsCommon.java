package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class FTBQuestsCommon {
	public void preInit() {
	}

	@Nullable
	public QuestFile getQuestFile(@Nullable World world) {
		return ServerQuestFile.INSTANCE;
	}

	@Nullable
	public QuestFile getQuestFile(boolean clientSide) {
		return ServerQuestFile.INSTANCE;
	}

	public void setTaskGuiProviders() {
	}

	public void setRewardGuiProviders() {
	}

	public String getLanguageCode() {
		return "en_us";
	}

	public void openCustomIconGui(ItemStack stack) {
	}

	@Nullable
	public QuestData getQuestData(@Nullable World world, UUID owner) {
		QuestFile file = getQuestFile(world);
		return file == null ? null : file.getData(owner);
	}
}