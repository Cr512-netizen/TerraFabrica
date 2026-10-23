package com.terrafabrica.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Панель экрана, нарисованная текстурой по схеме «9 срезов» (nine-slice).
 *
 * <p>Текстура панели — 48×48, то есть 3×3 тайла по 16 пикселей. Раскладка
 * (её же использует {@code tools/generate_textures.py}):
 *
 * <pre>
 *   (0,0)   (16,0)  (32,0)     ЛВ  верх  ПВ
 *   (0,16)  (16,16) (32,16)    лево центр право
 *   (0,32)  (16,32) (32,32)    ЛН  низ   ПН
 * </pre>
 *
 * <p>Углы рисуются по одному разу, рёбра и центр — тайлами с шагом 16, а
 * последний неполный тайл обрезается по размеру панели. Поэтому полосы рамки
 * в текстуре сделаны однородными по длине ребра: обрезка не видна. Готовые
 * {@code gui/machine.png} (176×166) и панели руководства/древа — из одного
 * генератора, никаких ванильных рамок не используется.
 */
public final class GuiPanel {

    /** Сторона одного тайла панели. */
    public static final int TILE = 16;

    /** Размер текстуры панели в пикселях (3×3 тайла). */
    private static final int TEXTURE_SIZE = 48;

    private GuiPanel() {}

    /**
     * Рисует панель с рамкой в прямоугольнике {@code (x, y, width, height)}.
     *
     * @param texture путь к текстуре 48×48 ({@code ns:textures/gui/<name>.png})
     */
    public static void draw(GuiGraphics graphics, ResourceLocation texture,
                            int x, int y, int width, int height) {
        if (width < TILE * 2 || height < TILE * 2) {
            // Слишком маленькая область — 9 срезов не имеют смысла, рисуем центр.
            graphics.blit(texture, x, y, (float) TILE, (float) TILE,
                    Math.max(1, width), Math.max(1, height), TEXTURE_SIZE, TEXTURE_SIZE);
            return;
        }
        int right = x + width - TILE;
        int bottom = y + height - TILE;

        // Углы.
        part(graphics, texture, x, y, 0, 0, TILE, TILE);
        part(graphics, texture, right, y, TILE * 2, 0, TILE, TILE);
        part(graphics, texture, x, bottom, 0, TILE * 2, TILE, TILE);
        part(graphics, texture, right, bottom, TILE * 2, TILE * 2, TILE, TILE);

        // Верхнее и нижнее рёбра.
        for (int dx = x + TILE; dx < right; dx += TILE) {
            int w = Math.min(TILE, right - dx);
            part(graphics, texture, dx, y, TILE, 0, w, TILE);
            part(graphics, texture, dx, bottom, TILE, TILE * 2, w, TILE);
        }
        // Левое и правое рёбра.
        for (int dy = y + TILE; dy < bottom; dy += TILE) {
            int h = Math.min(TILE, bottom - dy);
            part(graphics, texture, x, dy, 0, TILE, TILE, h);
            part(graphics, texture, right, dy, TILE * 2, TILE, TILE, h);
        }
        // Внутренность.
        for (int dy = y + TILE; dy < bottom; dy += TILE) {
            int h = Math.min(TILE, bottom - dy);
            for (int dx = x + TILE; dx < right; dx += TILE) {
                int w = Math.min(TILE, right - dx);
                part(graphics, texture, dx, dy, TILE, TILE, w, h);
            }
        }
    }

    private static void part(GuiGraphics graphics, ResourceLocation texture,
                             int dx, int dy, int u, int v, int width, int height) {
        graphics.blit(texture, dx, dy, (float) u, (float) v, width, height,
                TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
