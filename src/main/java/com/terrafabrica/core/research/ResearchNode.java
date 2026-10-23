package com.terrafabrica.core.research;

import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * Узел древа исследований Terra Fabrica.
 *
 * <p>Узел описывает: эпоху, предшественников (prerequisites), стоимость в
 * ресурсах (изымаются из инвентаря игрока при старте), время исследования
 * в тиках и позицию в GUI (в условных ячейках сетки).
 *
 * <p>Названия и описания — через ключи локализации:
 * {@code research.terrafabrica.<id>} и {@code research.terrafabrica.<id>.desc}.
 *
 * <p>Граф должен быть связным и без тупиков: любой узел достижим от корня
 * (узлы без предшественников — корни Эпохи I), и каждый узел либо финальный,
 * либо входит в предшественники другого узла. Проверка выполняется в
 * {@link ResearchTree#validateGraph()}.
 */
public final class ResearchNode {

    /** Стоимость в ресурсах: предмет + количество. */
    public record ItemCost(ItemLike item, int count) {}

    private final String id;
    private final int era;
    private final List<String> prerequisites;
    private final long timeTicks;
    private final List<ItemCost> costs;
    private final float posX;
    private final float posY;

    public ResearchNode(String id, int era, List<String> prerequisites, long timeTicks,
                        List<ItemCost> costs, float posX, float posY) {
        this.id = id;
        this.era = era;
        this.prerequisites = List.copyOf(prerequisites);
        this.timeTicks = timeTicks;
        this.costs = List.copyOf(costs);
        this.posX = posX;
        this.posY = posY;
    }

    public String id() {
        return id;
    }

    /** 1 — Эпоха I, 2 — Эпоха II. */
    public int era() {
        return era;
    }

    /** ID узлов-предшественников. */
    public List<String> prerequisites() {
        return prerequisites;
    }

    /** Базовая длительность исследования, тик. */
    public long timeTicks() {
        return timeTicks;
    }

    /** Ресурсы, изымаемые при старте исследования. */
    public List<ItemCost> costs() {
        return costs;
    }

    public float posX() {
        return posX;
    }

    public float posY() {
        return posY;
    }

    public String langKey() {
        return "research.terrafabrica." + id;
    }

    public String langDescKey() {
        return "research.terrafabrica." + id + ".desc";
    }
}
