package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class QuestObject
{
	public String id = "";
	public boolean invalid = false;
	public String title = "";
	public ItemStack icon = ItemStack.EMPTY;
	private Icon cachedIcon = null;

	public abstract QuestFile getQuestFile();

	public abstract QuestObjectType getObjectType();

	public abstract String getID();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getAltIcon();

	public abstract long getProgress(IProgressData data);

	public abstract long getMaxProgress();

	public abstract void resetProgress(IProgressData data);

	public abstract void completeInstantly(IProgressData data);

	public abstract double getRelativeProgress(IProgressData data);

	public abstract boolean isComplete(IProgressData data);

	public Icon getIcon()
	{
		if (cachedIcon == null)
		{
			if (!icon.isEmpty())
			{
				cachedIcon = ItemIcon.getItemIcon(icon);
			}
			else
			{
				cachedIcon = getAltIcon();
			}
		}

		return cachedIcon;
	}

	public abstract ITextComponent getAltDisplayName();

	public final ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.equals("-") ? "" : title);
		}

		return getAltDisplayName();
	}

	public void deleteSelf()
	{
		getQuestFile().remove(getID());
		invalid = true;
	}

	public void deleteChildren()
	{
	}

	public void onCreated()
	{
	}

	@Override
	public final String toString()
	{
		return getID();
	}

	@Override
	public final int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public final boolean equals(Object o)
	{
		return o == this;
	}

	public void getConfig(ConfigGroup config)
	{
	}

	public void getExtraConfig(ConfigGroup config)
	{
		config.add("title", new ConfigString(title)
		{
			@Override
			public String getString()
			{
				return title;
			}

			@Override
			public void setString(String v)
			{
				title = v;
			}
		}, new ConfigString("")).setDisplayName(new TextComponentTranslation("ftbquests.title")).setOrder((byte) -127);

		config.add("icon", new ConfigItemStack(icon, true)
		{
			@Override
			public ItemStack getStack()
			{
				return icon;
			}

			@Override
			public void setStack(ItemStack v)
			{
				icon = v;
			}
		}, new ConfigItemStack(ItemStack.EMPTY)).setDisplayName(new TextComponentTranslation("ftbquests.icon")).setOrder((byte) -126);
	}

	public void readID(NBTTagCompound nbt)
	{
		id = nbt.getString("id");

		if (id.isEmpty())
		{
			id = StringUtils.getId(getDisplayName().getUnformattedText(), StringUtils.FLAG_ID_DEFAULTS);
		}

		if (id.length() > 32)
		{
			id = id.substring(0, 32);
		}

		if (id.isEmpty() || getQuestFile().get(getID()) != null)
		{
			id = StringUtils.fromUUID(UUID.randomUUID());
		}
	}

	public void clearCachedData()
	{
		cachedIcon = null;
	}
}