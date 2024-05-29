package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class RenderProgressScreen extends TileEntitySpecialRenderer<TileProgressScreenCore> {
    public static final Color4I COLOR_NOT_STARTED = Color4I.rgb(0xFF4747);
    public static final Color4I COLOR_IN_PROGRESS = Color4I.rgb(0xFFDF42);
    public static final Color4I COLOR_COMPLETED = Color4I.rgb(0x68D341);

    @Override
    public void render(TileProgressScreenCore screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!ClientQuestFile.exists()) {
            return;
        }

        Chapter chapter = ClientQuestFile.INSTANCE.getChapter(screen.chapter);

        if (chapter == null) {
            return;
        }

        QuestData team = ClientQuestFile.INSTANCE.getData(screen.team);

        if (team == null) {
            return;
        }

        GlStateManager.color(1F, 1F, 1F, alpha);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0F, 1F, 0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(screen.getFacing().getHorizontalAngle() + 180F, 0F, 1F, 0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        setLightmapDisabled(true);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        FontRenderer font = getFontRenderer();
        boolean flag = font.getUnicodeFlag();
        font.setUnicodeFlag(true);

        int size = Math.min(screen.width * 2, screen.height);
        GlStateManager.translate(-size / 2F, -size, -0.001F);
        GlStateManager.scale(size + 1F, size + 1F, 1F);

        if (!screen.fullscreen) {
            drawString(font, chapter.getYellowDisplayName() + TextFormatting.RESET + " | " + team.getDisplayName().getFormattedText(), 0.02, 0.12);
            drawString(font, chapter.getRelativeProgress(team) + "%", 0.86, 0.12);
        }

        if (!chapter.quests.isEmpty()) {
            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

            for (Quest quest : chapter.quests) {
                minX = Math.min(minX, quest.x - quest.size / 2D);
                minY = Math.min(minY, quest.y - quest.size / 2D);
                maxX = Math.max(maxX, quest.x + quest.size / 2D);
                maxY = Math.max(maxY, quest.y + quest.size / 2D);
            }

            double sizeX = maxX - minX;
            double sizeY = maxY - minY;

            if (!screen.fullscreen) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.15F, 0.15F, 0F);
                GlStateManager.scale(0.7F, 0.7F, 1F);
            }

            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            int r, g, b, a = 255;

            for (Quest quest : chapter.quests) {
                int p = quest.getRelativeProgress(team);

                if (p == 0) {
                    r = COLOR_NOT_STARTED.redi();
                    g = COLOR_NOT_STARTED.greeni();
                    b = COLOR_NOT_STARTED.bluei();
                } else if (p == 100) {
                    r = COLOR_COMPLETED.redi();
                    g = COLOR_COMPLETED.greeni();
                    b = COLOR_COMPLETED.bluei();
                } else {
                    r = COLOR_IN_PROGRESS.redi();
                    g = COLOR_IN_PROGRESS.greeni();
                    b = COLOR_IN_PROGRESS.bluei();
                }

                double rw = (1D / sizeX - 0.005D) * quest.size;
                double rh = (1D / sizeY - 0.005D) * quest.size;
                double rx = (quest.x - minX) / sizeX + 0.0025D - rw / 2D;
                double ry = (quest.y - minY) / sizeY + 0.0025D - rh / 2D;

                buffer.pos(rx, ry, 0).color(r, g, b, a).endVertex();
                buffer.pos(rx, ry + rh, 0).color(r, g, b, a).endVertex();
                buffer.pos(rx + rw, ry + rh, 0).color(r, g, b, a).endVertex();
                buffer.pos(rx + rw, ry, 0).color(r, g, b, a).endVertex();
            }

            tessellator.draw();

            GlStateManager.enableTexture2D();

            double d = 3D * (size + 1D);

            if (!screen.hideIcons && Minecraft.getMinecraft().player.getDistanceSq(screen.getPos().getX() + 0.5D, screen.getPos().getY() + 0.5D + screen.height / 2D, screen.getPos().getZ() + 0.5D) <= d * d) {
                for (Quest quest : chapter.quests) {
                    double s = Math.max(sizeX, sizeY);
                    double rw = (0.75D / s) * quest.size;
                    double rh = (0.75D / s) * quest.size;
                    double rx = (quest.x - minX) / sizeX;
                    double ry = (quest.y - minY) / sizeY;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(rx, ry, -0.009F);
                    GlStateManager.scale(rw, rh, 1F);
                    quest.getIcon().draw3D();
                    GlStateManager.popMatrix();
                }
            }

            if (!screen.fullscreen) {
                GlStateManager.popMatrix();
            }
        }

        font.setUnicodeFlag(flag);
        setLightmapDisabled(false);
        GlStateManager.enableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }

    private void drawString(FontRenderer font, String string, double y, double size) {
        if (string.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5D, y, 0D);
        int len = font.getStringWidth(string);
        double scale = size / 9D;
        double w = len * scale;

        if (w > 1D) {
            scale /= w;
            w = 1D;
        }

        if (w > 0.9D) {
            scale *= 0.9D;
        }

        GlStateManager.scale(scale, scale, 1D);
        font.drawString(string, -len / 2, 0, 0xFFD8D8D8);
        GlStateManager.popMatrix();
    }
}