package com.terrafabrica.content.era1;

/**
 * Уровни богатства породы.
 *
 * <p>Визуально: бедная — 2–3 мелкие точки; обычная — 5–8 точек; богатая —
 * 10–15 точек и лёгкий отблеск. Добыча без Silk Touch: 1/2/3 куска породы,
 * Fortune увеличивает количество кусков (множитель 1 + уровень, через loot table).
 */
public enum PorodeRichness {

    POOR("poor", 1),
    NORMAL("normal", 2),
    RICH("rich", 3);

    /** Часть id блока: {@code copper_porode_poor}. */
    public final String suffix;
    /** Базовое число кусков породы при добыче (до Fortune). */
    public final int baseChunks;

    PorodeRichness(String suffix, int baseChunks) {
        this.suffix = suffix;
        this.baseChunks = baseChunks;
    }
}
