package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.io.DataReader;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.edit.MessageChangeProgressResponse;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.QuestObjectText;
import com.latmod.mods.itemfilters.item.ItemMissing;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestObjectBase {
	private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9_]*$");

	public static boolean isNull(@Nullable QuestObjectBase object) {
		return object == null || object.invalid;
	}

	public static int getID(@Nullable QuestObjectBase object) {
		return isNull(object) ? 0 : object.id;
	}

	public static String getCodeString(int id) {
		return String.format("%08x", id);
	}

	public static String getCodeString(@Nullable QuestObjectBase object) {
		return String.format("%08x", getID(object));
	}

	public int id = 0;
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	private Set<String> tags = new LinkedHashSet<>(0);

	private Icon cachedIcon = null;
	private String cachedTitle = null;
	private QuestObjectText cachedTextFile = null;

	public final String getCodeString() {
		return getCodeString(id);
	}

	public final String toString() {
		return getCodeString();
	}

	public final boolean equals(Object object) {
		return object == this;
	}

	public final int hashCode() {
		return id;
	}

	public abstract QuestObjectType getObjectType();

	public abstract QuestFile getQuestFile();

	public Set<String> getTags() {
		return tags;
	}

	public boolean hasTag(String tag) {
		return !tags.isEmpty() && tags.contains(tag);
	}

	public void changeProgress(QuestData data, ChangeProgress type) {
	}

	public void forceProgress(QuestData data, ChangeProgress type, boolean notifications) {
		ChangeProgress.sendUpdates = false;
		ChangeProgress.sendNotifications = notifications ? EnumTristate.TRUE : EnumTristate.FALSE;
		changeProgress(data, type);
		ChangeProgress.sendUpdates = true;
		ChangeProgress.sendNotifications = EnumTristate.DEFAULT;
		getQuestFile().clearCachedProgress();

		if (!getQuestFile().isClient()) {
			new MessageChangeProgressResponse(data.getTeamUID(), id, type, notifications).sendToAll();
		}

		data.markDirty();
	}

	@Nullable
	public Chapter getQuestChapter() {
		return null;
	}

	public int getParentID() {
		return 1;
	}

	public void writeData(NBTTagCompound nbt) {
		if (!title.isEmpty()) {
			nbt.setString("title", title);
		}

		if (!icon.isEmpty()) {
			nbt.setTag("icon", ItemMissing.write(icon, false));
		}

		if (!tags.isEmpty()) {
			NBTTagList tagList = new NBTTagList();

			for (String s : tags) {
				tagList.appendTag(new NBTTagString(s));
			}

			nbt.setTag("tags", tagList);
		}
	}

	public void readData(NBTTagCompound nbt) {
		title = nbt.getString("title");
		icon = ItemMissing.read(nbt.getTag("icon"));

		NBTTagList tagsList = nbt.getTagList("tags", Constants.NBT.TAG_STRING);

		tags = new LinkedHashSet<>(tagsList.tagCount());

		for (int i = 0; i < tagsList.tagCount(); i++) {
			tags.add(tagsList.getStringTagAt(i));
		}

		if (nbt.hasKey("custom_id")) {
			tags.add(nbt.getString("custom_id"));
		}
	}

	public void writeNetData(DataOut data) {
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !title.isEmpty());
		flags = Bits.setFlag(flags, 2, !icon.isEmpty());
		flags = Bits.setFlag(flags, 4, !tags.isEmpty());

		data.writeVarInt(flags);

		if (!title.isEmpty()) {
			data.writeString(title);
		}

		if (!icon.isEmpty()) {
			data.writeItemStack(icon);
		}

		if (!tags.isEmpty()) {
			data.writeCollection(tags, DataOut.STRING);
		}
	}

	public void readNetData(DataIn data) {
		int flags = data.readVarInt();
		title = Bits.getFlag(flags, 1) ? data.readString() : "";
		icon = Bits.getFlag(flags, 2) ? data.readItemStack() : ItemStack.EMPTY;
		tags = new LinkedHashSet<>(0);

		if (Bits.getFlag(flags, 4)) {
			data.readCollection(tags, DataIn.STRING);
		}
	}

	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config) {
		config.addString("title", () -> title, v -> title = v, "").setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder(-127);
		config.add("icon", new ConfigItemStack.SimpleStack(() -> icon, v -> icon = v), new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder(-126);
		config.addList("tags", tags, new ConfigString("", TAG_PATTERN), value -> new ConfigString(value, TAG_PATTERN), ConfigString::getString).setDisplayName(new TextComponentTranslation("ftbquests.tags")).setOrder(-125);
	}

	public QuestObjectText loadText() {
		if (invalid || id == 0) {
			return QuestObjectText.NONE;
		} else if (cachedTextFile == null) {
			cachedTextFile = QuestObjectText.NONE;
			File file = new File(Loader.instance().getConfigDir(), "ftbquests/" + getQuestFile().folderName + "/text/en_us/" + getCodeString(this) + ".txt");
			Map<String, String[]> text = new HashMap<>();

			if (file.exists()) {
				String currentKey = "";
				List<String> currentText = new ArrayList<>();

				for (String s : DataReader.get(file).safeStringList()) {
					if (s.indexOf('[') == 0 && s.indexOf(']') == s.length() - 1) {
						loadTextAdd(text, currentKey, currentText);
						currentKey = s.substring(1, s.length() - 1);
					} else {
						currentText.add(s);
					}
				}

				loadTextAdd(text, currentKey, currentText);
			}

			String langCode = FTBQuests.PROXY.getLanguageCode();

			if (!langCode.equals("en_us")) {
				File fileLang = new File(Loader.instance().getConfigDir(), "ftbquests/" + getQuestFile().folderName + "/text/" + langCode + "/" + getCodeString(this) + ".txt");

				if (fileLang.exists()) {
					String currentKey = "";
					List<String> currentText = new ArrayList<>();

					for (String s : DataReader.get(fileLang).safeStringList()) {
						if (s.indexOf('[') == 0 && s.indexOf(']') == s.length() - 1) {
							loadTextAdd(text, currentKey, currentText);
							currentKey = s.substring(1, s.length() - 1);
						} else {
							currentText.add(s);
						}
					}

					loadTextAdd(text, currentKey, currentText);
				}
			}

			cachedTextFile = text.isEmpty() ? QuestObjectText.NONE : new QuestObjectText(text);
		}

		return cachedTextFile;
	}

	private void loadTextAdd(Map<String, String[]> text, String currentKey, List<String> currentText) {
		while (!currentText.isEmpty() && currentText.get(0).isEmpty()) {
			currentText.remove(0);
		}

		while (!currentText.isEmpty() && currentText.get(currentText.size() - 1).isEmpty()) {
			currentText.remove(currentText.size() - 1);
		}

		if (!currentText.isEmpty()) {
			text.put(currentKey, currentText.toArray(new String[0]));
			currentText.clear();
		}
	}

	public abstract Icon getAltIcon();

	public abstract String getAltTitle();

	public final Icon getIcon() {
		if (cachedIcon != null) {
			return cachedIcon;
		}

		if (!icon.isEmpty()) {
			if (icon.getItem() == FTBQuestsItems.CUSTOM_ICON && icon.hasTagCompound()) {
				cachedIcon = Icon.getIcon(icon.getTagCompound().getString("icon"));
			} else {
				cachedIcon = ItemIcon.getItemIcon(icon);
			}
		}

		if (cachedIcon == null || cachedIcon.isEmpty()) {
			cachedIcon = ThemeProperties.ICON.get(this);
		}

		if (cachedIcon.isEmpty()) {
			cachedIcon = getAltIcon();
		}

		return cachedIcon;
	}

	public final String getTitle() {
		if (cachedTitle != null) {
			return cachedTitle;
		}

		String textTitle = loadText().getString("title");

		if (!textTitle.isEmpty()) {
			cachedTitle = textTitle;
			return cachedTitle;
		}

		String key = String.format("quests.%08x.title", id);
		String t = FTBQuestsClient.addI18nAndColors(I18n.format(key));

		if (t.isEmpty() || key.equals(t)) {
			if (!title.isEmpty()) {
				cachedTitle = FTBQuestsClient.addI18nAndColors(title);
			} else {
				cachedTitle = getAltTitle().trim();
			}
		} else {
			cachedTitle = t;
		}

		return cachedTitle;
	}

	public final String getUnformattedTitle() {
		return StringUtils.unformatted(getTitle());
	}

	public final String getYellowDisplayName() {
		return TextFormatting.YELLOW + getTitle();
	}

	public void deleteSelf() {
		getQuestFile().remove(id);
	}

	public void deleteChildren() {
	}

	@SideOnly(Side.CLIENT)
	public void editedFromGUI() {
		ClientQuestFile.INSTANCE.refreshGui();
	}

	public void onCreated() {
	}

	@Nullable
	public File getFile() {
		return null;
	}

	public void clearCachedData() {
		cachedIcon = null;
		cachedTitle = null;
		cachedTextFile = null;
	}

	public ConfigGroup createSubGroup(ConfigGroup group) {
		return group.getGroup(getObjectType().getId());
	}

	@SideOnly(Side.CLIENT)
	public void onEditButtonClicked() {
		ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
		getConfig(createSubGroup(group));
		new GuiEditConfig(group, (group1, sender) -> new MessageEditObject(this).sendToServer()).openGui();
	}

	public int refreshJEI() {
		return 0;
	}
}