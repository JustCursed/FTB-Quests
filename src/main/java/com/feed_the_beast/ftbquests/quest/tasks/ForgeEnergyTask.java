package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ForgeEnergyTask extends QuestTask
{
	private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_empty.png");
	private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_full.png");

	public final ConfigLong value;
	public final ConfigInt maxInput;

	public ForgeEnergyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		value = new ConfigLong(nbt.getLong("value"), 1, Long.MAX_VALUE);
		maxInput = new ConfigInt(nbt.hasKey("max_input") ? nbt.getInteger("max_input") : Integer.MAX_VALUE, 100, Integer.MAX_VALUE);
	}

	@Override
	public long getMaxProgress()
	{
		return value.getLong();
	}

	@Override
	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(value.getDouble(), true);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setLong("value", value.getLong());

		if (value.getInt() != Integer.MAX_VALUE)
		{
			nbt.setInteger("max_input", value.getInt());
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon(EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(FULL_TEXTURE.toString()));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task.ftbquests.forge_energy.text", StringUtils.formatDouble(value.getLong(), true));
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("value", value, new ConfigLong(1));
		group.add("max_input", maxInput, new ConfigInt(Integer.MAX_VALUE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(EMPTY_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + h, 0).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, 0).tex(1, 1).endVertex();
		buffer.pos(x + w, y, 0).tex(1, 0).endVertex();
		buffer.pos(x, y, 0).tex(0, 0).endVertex();
		tessellator.draw();

		double r = data == null ? 0D : data.getRelativeProgress();

		if (r > 0D)
		{
			double h1 = (r * 30D / 32D) * h;
			double y1 = y + (1D / 32D + (1D - r) * 30D / 32D) * h;

			double v0 = 1D / 32D + (30D / 32D) * (1D - r);
			double v1 = 31D / 32D;

			ClientUtils.MC.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y1 + h1, 0).tex(0, v1).endVertex();
			buffer.pos(x + w, y1 + h1, 0).tex(1, v1).endVertex();
			buffer.pos(x + w, y1, 0).tex(1, v0).endVertex();
			buffer.pos(x, y1, 0).tex(0, v0).endVertex();
			tessellator.draw();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(EMPTY_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		double x = -0.5;
		double y = -0.5;
		double w = 1;
		double h = 1;
		double z = 0;
		buffer.pos(x, y + h, z).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, z).tex(1, 1).endVertex();
		buffer.pos(x + w, y, z).tex(1, 0).endVertex();
		buffer.pos(x, y, z).tex(0, 0).endVertex();
		tessellator.draw();

		double r = data == null ? 0D : data.getRelativeProgress();

		if (r > 0D)
		{
			x -= 1D / 128D;
			w += 1D / 64D;

			h = r * 30D / 32D;
			y = 1D / 32D + (1D - r) * 30D / 32D - 0.5;

			y -= 1D / 128D;
			h += 1D / 64D;
			z = -0.003D;

			double u0 = 0;
			double v0 = 1D / 32D + (30D / 32D) * (1D - r);
			double u1 = 1;
			double v1 = 31D / 32D;

			ClientUtils.MC.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).endVertex();
			tessellator.draw();
		}
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ForgeEnergyTask> implements IEnergyStorage
	{
		private Data(ForgeEnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(progress, true);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityEnergy.ENERGY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityEnergy.ENERGY ? (T) this : null;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if (maxReceive > 0 && progress < task.value.getLong())
			{
				long add = Math.min(task.maxInput.getInt(), Math.min(maxReceive, task.value.getLong() - progress));

				if (add > 0L)
				{
					if (!simulate)
					{
						progress += add;
						data.syncTask(this);
					}

					return (int) add;
				}
			}

			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			return 0;
		}

		@Override
		public int getMaxEnergyStored()
		{
			return task.maxInput.getInt();
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}
	}
}