package com.feed_the_beast.ftbquests.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.EnergyTask;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MJTask extends EnergyTask {
    public static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_empty.png");
    public static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_full.png");

    public MJTask(Quest quest) {
        super(quest);
        value = 10000000000L;
    }

    @Override
    public TaskType getType() {
        return BuildCraftIntegration.MJ_TASK;
    }

    @Override
    public String getMaxProgressString() {
        return StringUtils.formatDouble(value / 1000000D, true);
    }

    @Override
    public String getAltTitle() {
        return I18n.format("ftbquests.task.ftbquests.buildcraft_mj.text", StringUtils.formatDouble(value / 1000000D, true));
    }

    @Override
    public void drawScreen(@Nullable TaskData data) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Minecraft mc = Minecraft.getMinecraft();

        mc.getTextureManager().bindTexture(EMPTY_TEXTURE);
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

        double r = data == null ? 0D : data.progress / (double) data.task.getMaxProgress();

        if (r > 0D) {
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

            mc.getTextureManager().bindTexture(FULL_TEXTURE);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
            buffer.pos(x + w, y + h, z).tex(u1, v1).endVertex();
            buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
            buffer.pos(x, y, z).tex(u0, v0).endVertex();
            tessellator.draw();
        }
    }

    @Override
    public TaskData createData(QuestData data) {
        return new Data(this, data);
    }

    public static class Data extends TaskData<MJTask> implements IMjReceiver {
        private Data(MJTask task, QuestData data) {
            super(task, data);
        }

        @Override
        public String getProgressString() {
            return StringUtils.formatDouble(progress / 1000000D, true);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR;
        }

        @Override
        @Nullable
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR ? (T) this : null;
        }

        @Override
        public long getPowerRequested() {
            long add = task.value - progress;

            if (task.maxInput > 0) {
                add = Math.min(add, task.maxInput);
            }

            return add;
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            if (microJoules > 0L && !isComplete()) {
                long add = Math.min(microJoules, getPowerRequested());

                if (add > 0L) {
                    if (!simulate) {
                        addProgress(add);
                    }

                    return microJoules - add;
                }
            }

            return microJoules;
        }

        @Override
        public boolean canConnect(@Nonnull IMjConnector other) {
            return true;
        }
    }
}