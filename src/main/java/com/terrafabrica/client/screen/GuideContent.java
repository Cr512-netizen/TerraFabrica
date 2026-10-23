package com.terrafabrica.client.screen;

import java.util.List;

/**
 * Содержание внутриигрового руководства.
 *
 * <p>Текст лежит в lang-файлах ({@code guide.terrafabrica.<глава>.line.<N>}), поэтому
 * руководство переводится вместе с модом, а не хардкодится в коде. Здесь описаны
 * только структура и порядок глав — правка текста не требует пересборки Java.
 */
public final class GuideContent {

    /** Глава: идентификатор (для ключей lang) и число строк. */
    public record Chapter(String id, int lines) {
        public String titleKey() {
            return "guide.terrafabrica." + id + ".title";
        }

        public String lineKey(int index) {
            return "guide.terrafabrica." + id + ".line." + index;
        }
    }

    private static final List<Chapter> CHAPTERS = List.of(
            new Chapter("intro", 6),
            new Chapter("units", 7),
            new Chapter("porodes", 6),
            new Chapter("era1", 7),
            new Chapter("era2", 8),
            new Chapter("research", 4),
            new Chapter("datapack", 5));

    private GuideContent() {}

    public static List<Chapter> chapters() {
        return CHAPTERS;
    }

    public static Chapter chapter(int index) {
        return CHAPTERS.get(Math.max(0, Math.min(CHAPTERS.size() - 1, index)));
    }

    public static int count() {
        return CHAPTERS.size();
    }
}
