package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.*;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ImageIcon;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateTaskAt;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.ChapterImage;
import com.feed_the_beast.ftbquests.quest.Movable;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PanelQuests extends Panel {
    private static final ImageIcon DEFAULT_DEPENDENCY_LINE_TEXTURE = (ImageIcon) Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/dependency.png");
    public final GuiQuestTree treeGui;
    public double questX = 0;
    public double questY = 0;
    public double centerQuestX = 0;
    public double centerQuestY = 0;
    public ButtonQuest mouseOverQuest = null;
    public double questMinX, questMinY, questMaxX, questMaxY;

    public PanelQuests(Panel panel) {
        super(panel);
        treeGui = (GuiQuestTree) panel.getGui();
    }

    public void updateMinMax() {
        questMinX = Double.POSITIVE_INFINITY;
        questMinY = Double.POSITIVE_INFINITY;
        questMaxX = Double.NEGATIVE_INFINITY;
        questMaxY = Double.NEGATIVE_INFINITY;

        for (Widget w : widgets) {
            double qx, qy, qw, qh;

            if (w instanceof ButtonQuest) {
                Quest q = ((ButtonQuest) w).quest;
                qx = q.x;
                qy = q.y;
                qw = q.size;
                qh = q.size;
            } else if (w instanceof ButtonChapterImage) {
                ChapterImage q = ((ButtonChapterImage) w).chapterImage;
                qx = q.x;
                qy = q.y;
                qw = q.width;
                qh = q.height;
            } else {
                continue;
            }

            questMinX = Math.min(questMinX, qx - qw / 2D);
            questMinY = Math.min(questMinY, qy - qh / 2D);
            questMaxX = Math.max(questMaxX, qx + qw / 2D);
            questMaxY = Math.max(questMaxY, qy + qh / 2D);
        }

        if (questMinX == Double.POSITIVE_INFINITY) {
            questMinX = questMinY = questMaxX = questMaxY = 0D;
        }

        questMinX -= 40D;
        questMinY -= 30D;
        questMaxX += 40D;
        questMaxY += 30D;
    }

    public void scrollTo(double x, double y) {
        updateMinMax();

        double dx = (questMaxX - questMinX);
        double dy = (questMaxY - questMinY);

        setScrollX((x - questMinX) / dx * treeGui.scrollWidth - width / 2D);
        setScrollY((y - questMinY) / dy * treeGui.scrollHeight - height / 2D);
    }

    public void resetScroll() {
        alignWidgets();
        setScrollX((treeGui.scrollWidth - width) / 2D);
        setScrollY((treeGui.scrollHeight - height) / 2D);
    }

    @Override
    public void addWidgets() {
        if (treeGui.selectedChapter == null) {
            return;
        }

        for (ChapterImage image : treeGui.selectedChapter.images) {
            if (!image.dev || treeGui.file.canEdit()) {
                add(new ButtonChapterImage(this, image));
            }
        }

        for (Quest quest : treeGui.selectedChapter.quests) {
            if (treeGui.file.canEdit() || quest.isVisible(ClientQuestFile.INSTANCE.self)) {
                add(new ButtonQuest(this, quest));
            }
        }

        alignWidgets();
    }

    @Override
    public void alignWidgets() {
        if (treeGui.selectedChapter == null) {
            return;
        }

        treeGui.scrollWidth = 0D;
        treeGui.scrollHeight = 0D;

        updateMinMax();

        double bs = treeGui.getQuestButtonSize();
        double bp = treeGui.getQuestButtonSpacing();

        treeGui.scrollWidth = (questMaxX - questMinX) * (bs + bp);
        treeGui.scrollHeight = (questMaxY - questMinY) * (bs + bp);

        for (Widget w : widgets) {
            double qx, qy, qw, qh;

            if (w instanceof ButtonQuest) {
                Quest q = ((ButtonQuest) w).quest;
                qx = q.x;
                qy = q.y;
                qw = q.size;
                qh = q.size;
            } else if (w instanceof ButtonChapterImage) {
                ChapterImage q = ((ButtonChapterImage) w).chapterImage;
                qx = q.x;
                qy = q.y;
                qw = q.width;
                qh = q.height;
            } else {
                continue;
            }

            double x = (qx - questMinX - qw / 2D) * (bs + bp) + bp / 2D + bp * (qw - 1D) / 2D;
            double y = (qy - questMinY - qh / 2D) * (bs + bp) + bp / 2D + bp * (qh - 1D) / 2D;
            w.setPosAndSize((int) x, (int) y, (int) (bs * qw), (int) (bs * qh));
        }

        setPosAndSize(20, 1, treeGui.width - 40, treeGui.height - 2);
    }

    @Override
    public void drawOffsetBackground(Theme theme, int x, int y, int w, int h) {
        if (treeGui.selectedChapter == null) {
            return;
        }

        for (Widget widget : widgets) {
            if (widget instanceof ButtonChapterImage) {
                widget.draw(theme, widget.getX(), widget.getY(), widget.width, widget.height);
            }
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Icon icon = ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(treeGui.selectedChapter);

        if (icon instanceof ImageIcon) {
            icon.bindTexture();
        } else {
            DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
        }

        Quest selectedQuest = treeGui.getViewedQuest();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        double mt = -(System.currentTimeMillis() * 0.001D);
        double mu = (mt * ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(treeGui.selectedChapter)) % 1D;
        double ms = (mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(treeGui.selectedChapter)) % 1D;
        double s = treeGui.getZoom() * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(treeGui.selectedChapter) / 4D * 3D;

        for (Widget widget : widgets) {
            if (!(widget instanceof ButtonQuest)) {
                continue;
            }

            Quest wquest = ((ButtonQuest) widget).quest;

            if (wquest.hideDependencyLines) {
                continue;
            }

            boolean unavailable = treeGui.file.self == null || !wquest.canStartTasks(treeGui.file.self);
            boolean complete = !unavailable && treeGui.file.self != null && wquest.isComplete(treeGui.file.self);

            for (ButtonQuest button : ((ButtonQuest) widget).getDependencies()) {
                if (button.quest == selectedQuest || wquest == selectedQuest) {
                    continue;
                }

                int r, g, b, a;

                if (complete) {
                    Color4I c = ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(treeGui.selectedChapter);
                    r = c.redi();
                    g = c.greeni();
                    b = c.bluei();
                    a = c.alphai();
                } else {
                    Color4I c = Color4I.hsb(button.quest.id / 1000F, 0.2F, unavailable ? 0.3F : 0.8F);
                    r = c.redi();
                    g = c.greeni();
                    b = c.bluei();
                    a = 180;
                }

                double sx = widget.getX() + widget.width / 2D;
                double sy = widget.getY() + widget.height / 2D;
                double ex = button.getX() + button.width / 2D;
                double ey = button.getY() + button.height / 2D;
                double len = MathUtils.dist(sx, sy, ex, ey);

                GlStateManager.pushMatrix();
                GlStateManager.translate(sx, sy, 0);
                GlStateManager.rotate((float) (Math.atan2(ey - sy, ex - sx) * 180D / Math.PI), 0F, 0F, 1F);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(0, -s, 0).tex(len / s / 2D + mu, 0).color(r, g, b, a).endVertex();
                buffer.pos(0, s, 0).tex(len / s / 2D + mu, 1).color(r, g, b, a).endVertex();
                buffer.pos(len, s, 0).tex(mu, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
                buffer.pos(len, -s, 0).tex(mu, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
        }

        for (Widget widget : widgets) {
            if (!(widget instanceof ButtonQuest)) {
                continue;
            }

            Quest wquest = ((ButtonQuest) widget).quest;

            if (wquest.hideDependencyLines) {
                continue;
            }

            for (ButtonQuest button : ((ButtonQuest) widget).getDependencies()) {
                int r, g, b, a;

                if (button.quest == selectedQuest) {
                    Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(treeGui.selectedChapter);
                    r = c.redi();
                    g = c.greeni();
                    b = c.bluei();
                    a = c.alphai();
                } else if (wquest == selectedQuest) {
                    Color4I c = ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(treeGui.selectedChapter);
                    r = c.redi();
                    g = c.greeni();
                    b = c.bluei();
                    a = c.alphai();
                } else {
                    continue;
                }

                double sx = widget.getX() + widget.width / 2D;
                double sy = widget.getY() + widget.height / 2D;
                double ex = button.getX() + button.width / 2D;
                double ey = button.getY() + button.height / 2D;
                double len = MathUtils.dist(sx, sy, ex, ey);

                GlStateManager.pushMatrix();
                GlStateManager.translate(sx, sy, 0);
                GlStateManager.rotate((float) (Math.atan2(ey - sy, ex - sx) * 180D / Math.PI), 0F, 0F, 1F);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(0, -s, 0).tex(len / s / 2D + ms, 0).color(r, g, b, a).endVertex();
                buffer.pos(0, s, 0).tex(len / s / 2D + ms, 1).color(r, g, b, a).endVertex();
                buffer.pos(len, s, 0).tex(ms, 1).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
                buffer.pos(len, -s, 0).tex(ms, 0).color(r * 3 / 4, g * 3 / 4, b * 3 / 4, a).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void draw(Theme theme, int x, int y, int w, int h) {
        super.draw(theme, x, y, w, h);

        if (treeGui.selectedChapter != null && isMouseOver()) {
            //updateMinMax();

            double dx = (questMaxX - questMinX);
            double dy = (questMaxY - questMinY);

            double px = getX() - getScrollX();
            double py = getY() - getScrollY();

            double qx = (treeGui.getMouseX() - px) / treeGui.scrollWidth * dx + questMinX;
            double qy = (treeGui.getMouseY() - py) / treeGui.scrollHeight * dy + questMinY;
            centerQuestX = (treeGui.width / 2D - px) / treeGui.scrollWidth * dx + questMinX;
            centerQuestY = (treeGui.height / 2D - py) / treeGui.scrollHeight * dy + questMinY;

            if (isShiftKeyDown()) {
                questX = qx;
                questY = qy;
            } else if (treeGui.selectedObjects.size() == 1 && treeGui.selectedObjects.get(0) instanceof Quest) {
                Quest q = (Quest) treeGui.selectedObjects.get(0);
                double s = (1D / treeGui.file.gridScale) / q.size;
                questX = MathHelper.floor(qx * s + 0.5D) / s;
                questY = MathHelper.floor(qy * s + 0.5D) / s;
            } else {
                double s = 1D / treeGui.file.gridScale;
                questX = MathHelper.floor(qx * s + 0.5D) / s;
                questY = MathHelper.floor(qy * s + 0.5D) / s;
            }

            if (treeGui.file.canEdit()) {
                theme.pushFontUnicode(true);
                GlStateManager.pushMatrix();
                GlStateManager.translate(0D, 0D, 1000D);
                theme.drawString("X:" + (questX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questX), x + 3, y + h - 18, Theme.SHADOW);
                theme.drawString("Y:" + (questY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(questY), x + 3, y + h - 10, Theme.SHADOW);
                theme.drawString("CX:" + (centerQuestX < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestX), x + w - 30, y + h - 18, Theme.SHADOW);
                theme.drawString("CY:" + (centerQuestY < 0 ? "" : " ") + StringUtils.DOUBLE_FORMATTER_00.format(centerQuestY), x + w - 30, y + h - 10, Theme.SHADOW);
                GlStateManager.popMatrix();
                theme.popFontUnicode();

                if (treeGui.movingObjects && !treeGui.selectedObjects.isEmpty()) {
                    double bs = treeGui.getQuestButtonSize();

                    double ominX = Double.POSITIVE_INFINITY, ominY = Double.POSITIVE_INFINITY, omaxX = Double.NEGATIVE_INFINITY, omaxY = Double.NEGATIVE_INFINITY;

                    for (Movable q : treeGui.selectedObjects) {
                        ominX = Math.min(ominX, q.getX());
                        ominY = Math.min(ominY, q.getY());
                        omaxX = Math.max(omaxX, q.getX());
                        omaxY = Math.max(omaxY, q.getY());
                    }

                    for (Movable m : treeGui.selectedObjects) {
                        double ox = m.getX() - ominX;
                        double oy = m.getY() - ominY;
                        double sx = (questX + ox - questMinX) / dx * treeGui.scrollWidth + px;
                        double sy = (questY + oy - questMinY) / dy * treeGui.scrollHeight + py;
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(sx - bs * m.getWidth() / 2D, sy - bs * m.getHeight() / 2D, 0D);
                        GlStateManager.scale(bs * m.getWidth(), bs * m.getHeight(), 1D);
                        GuiHelper.setupDrawing();
                        m.drawMoved();
                        GlStateManager.popMatrix();
                    }

                    if (GuiQuestTree.grid && treeGui.viewQuestPanel.quest == null) {
                        double boxX = ominX / dx * treeGui.scrollWidth + px;
                        double boxY = ominY / dy * treeGui.scrollHeight + py;
                        double boxW = omaxX / dx * treeGui.scrollWidth + px - boxX;
                        double boxH = omaxY / dy * treeGui.scrollHeight + py - boxY;

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0, 0, 1000);
                        GuiHelper.drawHollowRect((int) boxX, (int) boxY, (int) boxW, (int) boxH, Color4I.WHITE.withAlpha(30), false);
                        GlStateManager.popMatrix();
                    }
                } else if (treeGui.viewQuestPanel.quest == null || !treeGui.viewQuestPanel.isMouseOver()) {
                    int z = treeGui.getZoom();
                    double bs = treeGui.getQuestButtonSize();
                    double sx = (questX - questMinX) / dx * treeGui.scrollWidth + px;
                    double sy = (questY - questMinY) / dy * treeGui.scrollHeight + py;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(sx - bs / 2D, sy - bs / 2D, 0D);
                    GlStateManager.scale(bs, bs, 1D);
                    GuiHelper.setupDrawing();
                    QuestShape.get(treeGui.selectedChapter.getDefaultQuestShape()).shape.withColor(Color4I.WHITE.withAlpha(10)).draw(0, 0, 1, 1);
                    GlStateManager.popMatrix();

                    if (GuiQuestTree.grid && treeGui.viewQuestPanel.quest == null) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0, 0, 1000);
                        Color4I.WHITE.draw((int) sx, (int) sy, 1, 1);
                        Color4I.WHITE.withAlpha(30).draw(getX(), (int) sy, width, 1);
                        Color4I.WHITE.withAlpha(30).draw((int) sx, getY(), 1, height);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (treeGui.selectedChapter == null) {
            return false;
        }

        if (treeGui.movingObjects && treeGui.file.canEdit()) {
            if (treeGui.selectedChapter != null && !button.isRight() && !treeGui.selectedObjects.isEmpty()) {
                GuiHelper.playClickSound();

                double minX = Double.POSITIVE_INFINITY;
                double minY = Double.POSITIVE_INFINITY;

                for (Movable q : treeGui.selectedObjects) {
                    minX = Math.min(minX, q.getX());
                    minY = Math.min(minY, q.getY());
                }

                for (Movable q : treeGui.selectedObjects) {
                    q.move(treeGui.selectedChapter, questX + (q.getX() - minX), questY + (q.getY() - minY));
                }
            }

            treeGui.movingObjects = false;
            treeGui.selectedObjects.clear();
            return true;
        }

        if (super.mousePressed(button)) {
            return true;
        }

        if (!treeGui.viewQuestPanel.hidePanel && treeGui.getViewedQuest() != null) {
            treeGui.closeQuest();
            return true;
        }

        if (button.isLeft() && isMouseOver() && (treeGui.viewQuestPanel.hidePanel || treeGui.getViewedQuest() == null)) {
            treeGui.prevMouseX = getMouseX();
            treeGui.prevMouseY = getMouseY();
            treeGui.grabbed = 1;
            return true;
        }

        if (button.isRight() && treeGui.file.canEdit()) {
            GuiHelper.playClickSound();
            List<ContextMenuItem> contextMenu = new ArrayList<>();
            double qx = questX;
            double qy = questY;

            for (TaskType type : TaskType.getRegistry()) {
                contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
                    GuiHelper.playClickSound();
                    type.getGuiProvider().openCreationGui(this, new Quest(treeGui.selectedChapter), task -> new MessageCreateTaskAt(treeGui.selectedChapter, qx, qy, task).sendToServer());
                }));
            }

            contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.chapter.image"), GuiIcons.ART, () -> {
                GuiHelper.playClickSound();
                ChapterImage image = new ChapterImage(treeGui.selectedChapter);
                image.x = qx;
                image.y = qy;
                treeGui.selectedChapter.images.add(image);
                new MessageEditObject(treeGui.selectedChapter).sendToServer();
            }));

            treeGui.openContextMenu(contextMenu);
            return true;
        }

        return false;
    }

    @Override
    public void mouseReleased(MouseButton button) {
        super.mouseReleased(button);
        treeGui.grabbed = 0;
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        if (!treeGui.chapterHoverPanel.widgets.isEmpty()) {
            return false;
        }

        return super.checkMouseOver(mouseX, mouseY);
    }

    @Override
    public void updateMouseOver(int mouseX, int mouseY) {
        mouseOverQuest = null;
        super.updateMouseOver(mouseX, mouseY);

        for (Widget widget : widgets) {
            if (widget.isMouseOver() && widget instanceof ButtonQuest) {
                mouseOverQuest = (ButtonQuest) widget;
                break;
            }
        }
    }

    @Override
    public boolean scrollPanel(int scroll) {
        if (treeGui.selectedChapter != null && treeGui.getViewedQuest() == null && isMouseOver()) {
            treeGui.addZoom(scroll);
            return true;
        }

        return false;
    }
}