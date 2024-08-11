package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.edit.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject implements Movable {
	public Chapter chapter;
	public String subtitle;
	public double x, y;
	public boolean hide;
	public String shape;
	public final List<String> description;
	public final Set<QuestObject> dependencies;
	public boolean canRepeat;
	public final List<Task> tasks;
	public final List<Reward> rewards;
	public DependencyRequirement dependencyRequirement;
	public String guidePage;
	public String customClick;
	public boolean hideDependencyLines;
	public int minRequiredDependencies;
	public boolean hideTextUntilComplete;
	public EnumTristate disableJEI;
	public double size;
	public boolean optional;
	public int minWidth;
	public boolean orTasks;

	private String cachedDescription = null;
	private String[] cachedText = null;

	public Quest(Chapter c) {
		chapter = c;
		subtitle = "";
		x = 0;
		y = 0;
		shape = "";
		description = new ArrayList<>();
		canRepeat = false;
		dependencies = new HashSet<>(0);
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		guidePage = "";
		customClick = "";
		hideDependencyLines = false;
		hide = false;
		dependencyRequirement = DependencyRequirement.ALL_COMPLETED;
		minRequiredDependencies = 0;
		hideTextUntilComplete = false;
		disableJEI = EnumTristate.DEFAULT;
		size = 1D;
		optional = false;
		minWidth = 0;
		orTasks = false;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.QUEST;
	}

	@Override
	public QuestFile getQuestFile() {
		return chapter.file;
	}

	@Override
	public Chapter getQuestChapter() {
		return chapter;
	}

	@Override
	public int getParentID() {
		return chapter.id;
	}

	@Override
	public void writeData(NBTTagCompound nbt) {
		super.writeData(nbt);
		nbt.setDouble("x", x);
		nbt.setDouble("y", y);

		if (!shape.isEmpty()) {
			nbt.setString("shape", shape);
		}

		if (!subtitle.isEmpty()) {
			nbt.setString("description", subtitle);
		}

		if (!description.isEmpty()) {
			NBTTagList array = new NBTTagList();

			for (String value : description) {
				array.appendTag(new NBTTagString(value));
			}

			nbt.setTag("text", array);
		}

		if (canRepeat) {
			nbt.setBoolean("can_repeat", true);
		}

		if (!guidePage.isEmpty()) {
			nbt.setString("guide_page", guidePage);
		}

		if (!customClick.isEmpty()) {
			nbt.setString("custom_click", customClick);
		}

		if (hideDependencyLines) {
			nbt.setBoolean("hide_dependency_lines", true);
		}

		if (minRequiredDependencies > 0) {
			nbt.setInteger("min_required_dependencies", (byte) minRequiredDependencies);
		}

		removeInvalidDependencies();

		if (!dependencies.isEmpty()) {
			NBTTagList list = new NBTTagList();

			for (QuestObject dep : dependencies) {
				list.appendTag(new NBTTagString(dep.getCodeString()));
			}

			nbt.setTag("dependencies", list);
		}

		if (hide) {
			nbt.setBoolean("hide", true);
		}

		if (dependencyRequirement != DependencyRequirement.ALL_COMPLETED) {
			nbt.setString("dependency_requirement", dependencyRequirement.getId());
		}

		if (hideTextUntilComplete) {
			nbt.setBoolean("hide_text_until_complete", true);
		}

		if (size != 1D) {
			nbt.setDouble("size", size);
		}

		if (optional) {
			nbt.setBoolean("optional", true);
		}

		if (minWidth > 0) {
			nbt.setInteger("min_width", minWidth);
		}

		if (orTasks) {
			nbt.setBoolean("or_tasks", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt) {
		super.readData(nbt);
		subtitle = nbt.getString("description");
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		shape = nbt.getString("shape");

		if (shape.equals("default")) {
			shape = "";
		}

		description.clear();

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++) {
			description.add(list.getStringTagAt(k));
		}

		canRepeat = nbt.getBoolean("can_repeat");
		guidePage = nbt.getString("guide_page");
		customClick = nbt.getString("custom_click");
		hideDependencyLines = nbt.getBoolean("hide_dependency_lines");
		minRequiredDependencies = nbt.getInteger("min_required_dependencies");

		dependencies.clear();

		NBTBase depsTag = nbt.getTag("dependencies");

		if (depsTag instanceof NBTTagIntArray) {
			for (int i : nbt.getIntArray("dependencies")) {
				QuestObject object = chapter.file.get(i);

				if (object != null) {
					dependencies.add(object);
				}
			}
		} else if (depsTag instanceof NBTTagList) {
			list = (NBTTagList) depsTag;

			for (int i = 0; i < list.tagCount(); i++) {
				if (list.get(i) instanceof NBTTagString) {
					QuestObject object = chapter.file.get(chapter.file.getID(list.getStringTagAt(i)));

					if (object != null) {
						dependencies.add(object);
					}
				} else if (list.get(i) instanceof NBTTagCompound) {
					NBTTagCompound nbt1 = list.getCompoundTagAt(i);
					QuestObject object = chapter.file.get(nbt1.getInteger("id"));

					if (object != null) {
						dependencies.add(object);
					}
				}
			}
		} else {
			QuestObject object = chapter.file.get(nbt.getInteger("dependency"));

			if (object != null) {
				dependencies.add(object);
			}
		}

		hide = nbt.getBoolean("hide");
		dependencyRequirement = DependencyRequirement.NAME_MAP.get(nbt.getString("dependency_requirement"));
		hideTextUntilComplete = nbt.getBoolean("hide_text_until_complete");
		size = nbt.hasKey("size") ? nbt.getDouble("size") : 1D;
		optional = nbt.getBoolean("optional");
		minWidth = nbt.getInteger("min_width");
		orTasks = nbt.getBoolean("or_tasks");
	}

	@Override
	public void writeNetData(DataOut data) {
		super.writeNetData(data);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, canRepeat);
		flags = Bits.setFlag(flags, 2, hide);
		flags = Bits.setFlag(flags, 4, !guidePage.isEmpty());
		flags = Bits.setFlag(flags, 8, !subtitle.isEmpty());
		flags = Bits.setFlag(flags, 16, !description.isEmpty());
		flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		// dep list empty
		flags = Bits.setFlag(flags, 64, hideDependencyLines);
		flags = Bits.setFlag(flags, 128, hideTextUntilComplete);
		flags = Bits.setFlag(flags, 256, optional);
		flags = Bits.setFlag(flags, 512, minWidth > 0);
		flags = Bits.setFlag(flags, 1024, orTasks);
		data.writeVarInt(flags);

		if (!subtitle.isEmpty()) {
			data.writeString(subtitle);
		}

		data.writeDouble(x);
		data.writeDouble(y);
		data.writeString(shape);

		if (!description.isEmpty()) {
			data.writeCollection(description, DataOut.STRING);
		}

		if (!guidePage.isEmpty()) {
			data.writeString(guidePage);
		}

		if (!customClick.isEmpty()) {
			data.writeString(customClick);
		}

		data.writeVarInt(minRequiredDependencies);
		DependencyRequirement.NAME_MAP.write(data, dependencyRequirement);
		data.writeVarInt(dependencies.size());

		for (QuestObject d : dependencies) {
			if (d.invalid) {
				data.writeInt(0);
			} else {
				data.writeInt(d.id);
			}
		}

		data.writeDouble(size);

		if (minWidth > 0) {
			data.writeVarInt(minWidth);
		}
	}

	@Override
	public void readNetData(DataIn data) {
		super.readNetData(data);
		int flags = data.readVarInt();
		subtitle = Bits.getFlag(flags, 8) ? data.readString() : "";
		x = data.readDouble();
		y = data.readDouble();
		shape = data.readString();

		if (Bits.getFlag(flags, 16)) {
			data.readCollection(description, DataIn.STRING);
		} else {
			description.clear();
		}

		canRepeat = Bits.getFlag(flags, 1);
		hide = Bits.getFlag(flags, 2);
		guidePage = Bits.getFlag(flags, 4) ? data.readString() : "";
		customClick = Bits.getFlag(flags, 32) ? data.readString() : "";
		hideDependencyLines = Bits.getFlag(flags, 64);
		hideTextUntilComplete = Bits.getFlag(flags, 128);
		optional = Bits.getFlag(flags, 256);

		minRequiredDependencies = data.readVarInt();
		dependencyRequirement = DependencyRequirement.NAME_MAP.read(data);
		dependencies.clear();
		int d = data.readVarInt();

		for (int i = 0; i < d; i++) {
			QuestObject object = chapter.file.get(data.readInt());

			if (object != null) {
				dependencies.add(object);
			}
		}

		size = data.readDouble();
		minWidth = Bits.getFlag(flags, 512) ? data.readVarInt() : 0;
		orTasks = Bits.getFlag(flags, 1024);
	}

	@Override
	public int getRelativeProgressFromChildren(QuestData data) {
		/*if (data.getTimesCompleted(this) > 0)
		{
			return 100;
		}*/

		if (tasks.isEmpty()) {
			return areDependenciesComplete(data) ? 100 : 0;
		}

		if (orTasks && tasks.size() > 1) {
			if (!areDependenciesComplete(data)) {
				return 0;
			}

			for (Task task : tasks) {
				if (task.getRelativeProgress(data) >= 100) {
					return 100;
				}
			}

			return 0;
		}

		int progress = 0;

		for (Task task : tasks) {
			progress += task.getRelativeProgress(data);
		}

		if (progress > 0 && !areDependenciesComplete(data)) {
			return 0;
		}

		return getRelativeProgressFromChildren(progress, tasks.size());
	}

	public boolean areDependenciesComplete(QuestData data) {
		if (dependencies.isEmpty()) {
			return true;
		}

		if (data.areDependenciesCompleteCache == null) {
			data.areDependenciesCompleteCache = new Int2ByteOpenHashMap();
			data.areDependenciesCompleteCache.defaultReturnValue((byte) -1);
		}

		byte b = data.areDependenciesCompleteCache.get(id);

		if (b == -1) {
			b = areDependenciesComplete0(data) ? (byte) 1 : (byte) 0;
			data.areDependenciesCompleteCache.put(id, b);
		}

		return b == 1;
	}

	private boolean areDependenciesComplete0(QuestData data) {
		if (minRequiredDependencies > 0) {
			int complete = 0;

			for (QuestObject dependency : dependencies) {
				if (!dependency.invalid && dependency.isComplete(data)) {
					complete++;

					if (complete >= minRequiredDependencies) {
						return true;
					}
				}
			}

			return false;
		}

		if (dependencyRequirement.one) {
			for (QuestObject object : dependencies) {
				if (!object.invalid && (dependencyRequirement.completed ? object.isComplete(data) : object.isStarted(data))) {
					return true;
				}
			}

			return false;
		}

		for (QuestObject object : dependencies) {
			if (!object.invalid && (dependencyRequirement.completed ? !object.isComplete(data) : !object.isStarted(data))) {
				return false;
			}
		}

		return true;
	}

	public boolean canStartTasks(QuestData data) {
		return areDependenciesComplete(data);
	}

	@Override
	public void onCompleted(QuestData data, List<EntityPlayerMP> onlineMembers, List<EntityPlayerMP> notifiedPlayers) {
		//data.setTimesCompleted(this, data.getTimesCompleted(this) + 1);
		super.onCompleted(data, onlineMembers, notifiedPlayers);

		if (!disableToast) {
			for (EntityPlayerMP player : notifiedPlayers) {
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		data.checkAutoCompletion(this);
		new ObjectCompletedEvent.QuestEvent(data, this, onlineMembers, notifiedPlayers).post();

		for (Chapter chapter : chapter.file.chapters) {
			for (Quest quest : chapter.quests) {
				if (quest.dependencies.contains(this)) {
					data.checkAutoCompletion(quest);
				}
			}
		}

		if (chapter.isComplete(data)) {
			chapter.onCompleted(data, onlineMembers, notifiedPlayers);
		}
	}

	@Override
	public void changeProgress(QuestData data, ChangeProgress type) {
		//FIXME: data.setTimesCompleted(this, -1);

		if (type.dependencies) {
			for (QuestObject dependency : dependencies) {
				if (!dependency.invalid) {
					dependency.changeProgress(data, type);
				}
			}
		}

		for (Task task : tasks) {
			task.changeProgress(data, type);
		}

		if (type.reset) {
			data.unclaimRewards(rewards);
		}
	}

	@Override
	public Icon getAltIcon() {
		List<Icon> list = new ArrayList<>();

		for (Task task : tasks) {
			list.add(task.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public String getAltTitle() {
		if (!tasks.isEmpty()) {
			return tasks.get(0).getTitle();
		}

		return I18n.format("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf() {
		super.deleteSelf();
		chapter.quests.remove(this);
	}

	@Override
	public void deleteChildren() {
		for (Task task : tasks) {
			task.deleteChildren();
			task.invalid = true;
		}

		for (Reward reward : rewards) {
			reward.deleteChildren();
			reward.invalid = true;
		}

		tasks.clear();
		rewards.clear();
	}

	@Override
	public void onCreated() {
		chapter.quests.add(this);

		if (!tasks.isEmpty()) {
			for (Task task : ListUtils.clearAndCopy(tasks)) {
				task.onCreated();
			}
		}
	}

	@Override
	public File getFile() {
		return new File(chapter.file.getFolder(), "chapters/" + getCodeString(chapter) + "/" + getCodeString(this) + ".snbt");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("subtitle", () -> subtitle, v -> subtitle = v, "");
		config.addList("description", description, new ConfigString(""), ConfigString::new, ConfigString::getString);
		config.addEnum("shape", () -> shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		config.addBool("hide", () -> hide, v -> hide = v, false);
		config.addBool("can_repeat", () -> canRepeat, v -> canRepeat = v, false);
		config.addDouble("size", () -> size, v -> size = v, 1, 0.0625D, 8D);
		config.addDouble("x", () -> x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", () -> y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addBool("optional", () -> optional, v -> optional = v, false);
		config.addInt("min_width", () -> minWidth, v -> minWidth = v, 0, 0, 3000);

		if (tasks.size() > 1) {
			config.addBool("or_tasks", () -> orTasks, v -> orTasks = v, false);
		}

		Predicate<QuestObjectBase> depTypes = object -> object != chapter.file && object != chapter && object instanceof QuestObject;// && !(object instanceof Task);

		config.addList("dependencies", dependencies, new ConfigQuestObject(chapter.file, 0, depTypes), questObject -> new ConfigQuestObject(chapter.file, questObject.id, depTypes), configQuestObject -> chapter.file.get(configQuestObject.getObject())).setDisplayName(new TextComponentTranslation("ftbquests.dependencies"));
		config.addEnum("dependency_requirement", () -> dependencyRequirement, v -> dependencyRequirement = v, DependencyRequirement.NAME_MAP);
		config.addInt("min_required_dependencies", () -> minRequiredDependencies, v -> minRequiredDependencies = v, 0, 0, Integer.MAX_VALUE);
		config.addBool("hide_dependency_lines", () -> hideDependencyLines, v -> hideDependencyLines = v, false);
		config.addString("guide_page", () -> guidePage, v -> guidePage = v, "");
		config.addString("custom_click", () -> customClick, v -> customClick = v, "");
		config.addBool("hide_text_until_complete", () -> hideTextUntilComplete, v -> hideTextUntilComplete = v, false);
		config.addEnum("disable_jei", () -> disableJEI, v -> disableJEI = v, EnumTristate.NAME_MAP);
	}

	@Override
	public Chapter getChapter() {
		return chapter;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return size;
	}

	@Override
	public double getHeight() {
		return size;
	}

	@Override
	public String getShape() {
		return shape.isEmpty() ? chapter.getDefaultQuestShape() : shape;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void move(Chapter to, double x, double y) {
		new MessageMoveQuest(id, to.id, x, y).sendToServer();
	}

	public boolean isProgressionIgnored() {
		return canRepeat || optional || !customClick.isEmpty();
	}

	@Override
	public boolean isVisible(QuestData data) {
		if (dependencies.isEmpty()) {
			return true;
		}

		if (hide) {
			return areDependenciesComplete(data);
		}

		for (QuestObject object : dependencies) {
			if (object.isVisible(data)) {
				return true;
			}
		}

		return false;
	}

	public Task getTask(int index) {
		if (tasks.isEmpty()) {
			throw new IllegalStateException("Quest has no tasks!");
		} else if (index <= 0) {
			return tasks.get(0);
		} else if (index >= tasks.size()) {
			return tasks.get(tasks.size() - 1);
		}

		return tasks.get(index);
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		cachedDescription = null;
		cachedText = null;

		for (Task task : tasks) {
			task.clearCachedData();
		}

		for (Reward reward : rewards) {
			reward.clearCachedData();
		}
	}

	public String getSubtitle() {
		if (cachedDescription != null) {
			return cachedDescription;
		}

		String textDesc = loadText().getString("description");

		if (!textDesc.isEmpty()) {
			cachedDescription = textDesc;
			return cachedDescription;
		}

		String key = String.format("quests.%08x.description", id);
		String t = FTBQuestsClient.addI18nAndColors(I18n.format(key));

		if (t.isEmpty() || key.equals(t)) {
			cachedDescription = FTBQuestsClient.addI18nAndColors(subtitle);
		} else {
			cachedDescription = t;
		}

		return cachedDescription;
	}

	public String[] getDescription() {
		if (cachedText != null) {
			return cachedText;
		}

		cachedText = loadText().getStringArray("text");

		if (cachedText.length > 0) {
			return cachedText;
		}

		if (description.isEmpty()) {
			return StringUtils.EMPTY_ARRAY;
		}

		cachedText = new String[description.size()];

		for (int i = 0; i < cachedText.length; i++) {
			cachedText[i] = FTBQuestsClient.addI18nAndColors(description.get(i));
		}

		return cachedText;
	}

	public boolean hasDependency(QuestObject object) {
		if (object.invalid) {
			return false;
		}

		for (QuestObject dependency : dependencies) {
			if (dependency == object) {
				return true;
			}
		}

		return false;
	}

	private void removeInvalidDependencies() {
		if (!dependencies.isEmpty()) {
			dependencies.removeIf(o -> o == null || o.invalid || o == this);
		}
	}

	public boolean verifyDependencies(boolean autofix) {
		try {
			if (verifyDependenciesInternal(this, true)) {
				return true;
			}
		} catch (StackOverflowError error) {
		}

		if (autofix) {
			FTBQuests.LOGGER.error("Looping dependencies found in " + this + "! Deleting all dependencies...");
			dependencies.clear();

			if (!chapter.file.isClient()) {
				ServerQuestFile.INSTANCE.save();
			}
		} else {
			FTBQuests.LOGGER.error("Looping dependencies found in " + this + "!");
		}

		return false;
	}

	@Override
	public boolean verifyDependenciesInternal(QuestObject original, boolean firstLoop) {
		if (this == original && !firstLoop) {
			return false;
		}

		removeInvalidDependencies();

		for (QuestObject dependency : dependencies) {
			if (!dependency.verifyDependenciesInternal(original, false)) {
				return false;
			}
		}

		return true;
	}

	public void checkRepeatableQuests(QuestData data, UUID player) {
		if (!canRepeat) {
			return;
		}

		for (Reward reward1 : rewards) {
			if (!data.isRewardClaimed(player, reward1)) {
				return;
			}
		}

		changeProgress(data, ChangeProgress.RESET);
	}

	@Override
	public int refreshJEI() {
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void editedFromGUI() {
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null) {
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	public boolean hasUnclaimedRewards(UUID player, QuestData data, boolean showExcluded) {
		if (isComplete(data)) {
			for (Reward reward : rewards) {
				if ((showExcluded || reward.getExcludeFromClaimAll()) && !data.isRewardClaimed(player, reward)) {
					return true;
				}
			}
		}

		return false;
	}

	public int getUnclaimedRewards(UUID player, QuestData data, boolean showExcluded) {
		int r = 0;

		if (isComplete(data)) {
			for (Reward reward : rewards) {
				if ((showExcluded || !reward.getExcludeFromClaimAll()) && !data.isRewardClaimed(player, reward)) {
					r++;
				}
			}
		}

		return r;
	}

	public void moved(double nx, double ny, int nc) {
		x = nx;
		y = ny;

		if (nc != chapter.id) {
			QuestFile f = getQuestFile();
			Chapter c = f.getChapter(nc);

			if (c != null) {
				File oldFile = f.isClient() ? null : getFile();
				chapter.quests.remove(this);
				c.quests.add(this);
				chapter = c;
				File newFile = f.isClient() ? null : getFile();

				if (oldFile != null && newFile != null && !oldFile.renameTo(newFile)) {
					FTBQuests.LOGGER.error("Couldn't rename " + oldFile.getPath() + " to " + newFile.getPath());
				}
			}
		}
	}

	public List<QuestObject> getDependants() {
		List<QuestObject> list = new ArrayList<>();

		for (Chapter c : chapter.file.chapters) {
			for (Quest q : c.quests) {
				if (q.dependencies.contains(this)) {
					list.add(q);
				}
			}
		}

		return list;
	}
}