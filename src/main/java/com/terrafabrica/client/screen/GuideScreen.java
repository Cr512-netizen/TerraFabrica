package com.terrafabrica.client.screen;

import com.terrafabrica.TerraFabrica;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Внутриигровое руководство (своё, без Patchouli и без книги-текстур).
 *
 * <p>Слева — список глав, справа — текст с постраничной разбивкой по ширине экрана.
 * Кнопки снизу: «Архив», «Исследования», «Закрыть». Перелистывание доступно мышью
 * и стрелками клавиатуры. Экран не ставит игру на паузу — можно читать у станка.
 */
public class GuideScreen extends Screen {

    private static final int MARGIN = 16;
    private static final int CHAPTER_WIDTH = 132;
    private static final int LINE_HEIGHT = 10;
    private static final int BUTTON_HEIGHT = 18;

    /** «Переплёт» руководства: своя текстура 48×48, режется 9-слайсом. */
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(TerraFabrica.MODID, "textures/gui/guide.png");

    private int chapterIndex;
    private int page;

    /** Прямоугольники кнопок, вычисляемые при отрисовке (нужны для кликов). */
    private int prevX, prevY, nextX, nextY, closeX, closeY, researchX, researchY;
    private int buttonWidth;
    private int hoveredChapter = -1;

    public GuideScreen() {
        super(Component.translatable("screen.terrafabrica.guide"));
    }

    private GuideContent.Chapter chapter() {
        return GuideContent.chapter(chapterIndex);
    }

    /** Строки главы, разбитые по ширине области текста. */
    private List<FormattedCharSequence> wrappedLines(int textWidth) {
        List<FormattedCharSequence> out = new ArrayList<>();
        GuideContent.Chapter chapter = chapter();
        for (int i = 1; i <= chapter.lines(); i++) {
            out.addAll(font.split(Component.translatable(chapter.lineKey(i)), textWidth));
            out.add(FormattedCharSequence.EMPTY);
        }
        return out;
    }

    private int linesPerPage(int textHeight) {
        return Math.max(1, textHeight / LINE_HEIGHT);
    }

    private int pageCount(int textWidth, int textHeight) {
        int total = wrappedLines(textWidth).size();
        int perPage = linesPerPage(textHeight);
        return Math.max(1, (total + perPage - 1) / perPage);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Фон рисуем сами и super.render НЕ зовём: в 1.21.1 Screen.render сам вызывает
        // renderBackground, то есть super.render после нашей панели затёр бы её
        // (проверено в живом клиенте — экран становился ванильно-серым без ошибок в логе).
        renderBackground(graphics, mouseX, mouseY, partialTick);

        int panelX = MARGIN;
        int panelY = MARGIN;
        int panelW = width - MARGIN * 2;
        int panelH = height - MARGIN * 2;
        // «Бумага» руководства: своя панель gui/guide.png (9-слайс), а не ванильный фон.
        GuiPanel.draw(graphics, PANEL, panelX, panelY, panelW, panelH);

        int contentTop = panelY + 22;
        int textX = panelX + CHAPTER_WIDTH + 10;
        int textWidth = panelW - CHAPTER_WIDTH - 28;
        int textHeight = panelY + panelH - contentTop - BUTTON_HEIGHT - 18;

        graphics.drawCenteredString(font, title, panelX + panelW / 2, panelY + 8, 0xFFE8DFC0);
        graphics.fill(panelX + 8, panelY + 18, panelX + panelW - 8, panelY + 19, 0xFF5C5140);

        renderChapters(graphics, panelX + 6, contentTop, mouseX, mouseY);

        List<FormattedCharSequence> lines = wrappedLines(textWidth);
        int perPage = linesPerPage(textHeight);
        int pages = Math.max(1, (lines.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);

        graphics.drawString(font, Component.translatable(chapter().titleKey()), textX, contentTop - 12,
                0xFFFFE08A);
        int y = contentTop + 2;
        int from = page * perPage;
        for (int i = from; i < Math.min(lines.size(), from + perPage); i++) {
            graphics.drawString(font, lines.get(i), textX, y, 0xFFD8D2C0);
            y += LINE_HEIGHT;
        }

        renderButtons(graphics, panelX, panelY, panelW, panelH, pages, mouseX, mouseY);
    }

    private void renderChapters(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        hoveredChapter = -1;
        for (int i = 0; i < GuideContent.count(); i++) {
            GuideContent.Chapter chapter = GuideContent.chapter(i);
            int rowY = y + i * 14;
            boolean active = i == chapterIndex;
            boolean hovered = mouseX >= x && mouseX < x + CHAPTER_WIDTH - 8
                    && mouseY >= rowY && mouseY < rowY + 12;
            if (hovered) hoveredChapter = i;
            if (active) {
                graphics.fill(x - 2, rowY - 2, x + CHAPTER_WIDTH - 8, rowY + 11, 0x50E0C060);
            }
            int color = active ? 0xFFFFE08A : (hovered ? 0xFFFFFFFF : 0xFFA89F8A);
            graphics.drawString(font, Component.translatable(chapter.titleKey()), x, rowY, color);
        }
    }

    private void renderButtons(GuiGraphics graphics, int panelX, int panelY, int panelW, int panelH,
                               int pages, int mouseX, int mouseY) {
        int y = panelY + panelH - BUTTON_HEIGHT - 8;
        buttonWidth = 64;

        researchX = panelX + 8;
        researchY = y;
        drawButton(graphics, researchX, researchY, 84, Component.translatable("gui.terrafabrica.guide.research"), mouseX, mouseY);

        prevX = panelX + panelW / 2 - 74;
        prevY = y;
        drawButton(graphics, prevX, prevY, 30, Component.literal(" < "), mouseX, mouseY);

        nextX = panelX + panelW / 2 + 44;
        nextY = y;
        drawButton(graphics, nextX, nextY, 30, Component.literal(" > "), mouseX, mouseY);

        Component pageLabel = Component.translatable("gui.terrafabrica.guide.page", page + 1, pages);
        graphics.drawString(font, pageLabel, panelX + panelW / 2 - font.width(pageLabel) / 2,
                y + 5, 0xFFB8AF98);

        closeX = panelX + panelW - 8 - 64;
        closeY = y;
        drawButton(graphics, closeX, closeY, 64, Component.translatable("gui.terrafabrica.guide.close"), mouseX, mouseY);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int w, Component label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        int fill = hovered ? 0xFF6A5F45 : 0xFF413A2C;
        int border = hovered ? 0xFFE8DFC0 : 0xFF8A7B5A;
        graphics.fill(x, y, x + w, y + BUTTON_HEIGHT, border);
        graphics.fill(x + 1, y + 1, x + w - 1, y + BUTTON_HEIGHT - 1, fill);
        graphics.drawString(font, label, x + (w - font.width(label)) / 2, y + 5, 0xFFF0EAD6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, researchX, researchY, 84, BUTTON_HEIGHT)) {
                minecraft.setScreen(new ResearchScreen());
                return true;
            }
            if (inside(mouseX, mouseY, prevX, prevY, 30, BUTTON_HEIGHT)) {
                turnPage(-1);
                return true;
            }
            if (inside(mouseX, mouseY, nextX, nextY, 30, BUTTON_HEIGHT)) {
                turnPage(1);
                return true;
            }
            if (inside(mouseX, mouseY, closeX, closeY, 64, BUTTON_HEIGHT)) {
                onClose();
                return true;
            }
            if (hoveredChapter >= 0) {
                chapterIndex = hoveredChapter;
                page = 0;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void turnPage(int delta) {
        int textWidth = width - MARGIN * 2 - CHAPTER_WIDTH - 28;
        int textHeight = height - MARGIN * 2 - 22 - BUTTON_HEIGHT - 18;
        int pages = pageCount(textWidth, textHeight);
        int target = page + delta;
        if (target < 0) {
            // С предыдущей страницы уходим в конец прошлой главы.
            if (chapterIndex > 0) {
                chapterIndex--;
                page = Integer.MAX_VALUE;
            }
            return;
        }
        if (target >= pages) {
            if (chapterIndex < GuideContent.count() - 1) {
                chapterIndex++;
                page = 0;
            }
            return;
        }
        page = target;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263) {              // стрелка влево
            turnPage(-1);
            return true;
        }
        if (keyCode == 262) {              // стрелка вправо
            turnPage(1);
            return true;
        }
        if (keyCode >= 258 && keyCode <= 261) {   // стрелки вверх/вниз — главы
            chapterIndex = Math.max(0, Math.min(GuideContent.count() - 1,
                    chapterIndex + (keyCode == 264 ? 1 : -1)));
            page = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
