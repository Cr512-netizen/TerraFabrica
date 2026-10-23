package com.terrafabrica.core.research;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Персональный прогресс исследований игрока.
 *
 * <p>Хранится как attachment игрока (см. {@code TFAttachments.RESEARCH_PROGRESS}),
 * сериализуется в NBT и синхронизируется сервер → клиент пакетом
 * {@code ResearchSyncPayload}. Серверная логика авторитетна: все переходы
 * состояний выполняются только на сервере в {@link ResearchManager}.
 *
 * <p>Состояния узла:
 * <ul>
 *   <li>{@link NodeState#LOCKED} — предшественники не открыты;</li>
 *   <li>{@link NodeState#AVAILABLE} — можно начать;</li>
 *   <li>{@link NodeState#IN_PROGRESS} — идёт исследование;</li>
 *   <li>{@link NodeState#UNLOCKED} — открыт; рецепты узла разблокированы.</li>
 * </ul>
 */
public final class ResearchProgress implements INBTSerializable<CompoundTag> {

    public enum NodeState {
        LOCKED, AVAILABLE, IN_PROGRESS, UNLOCKED
    }

    private final Map<String, NodeState> states = new HashMap<>();
    private final Map<String, Integer> progressTicks = new HashMap<>();
    private final Map<String, Integer> totalTicks = new HashMap<>();

    /** Состояние узла (отсутствующий узел = LOCKED). */
    public NodeState getState(String nodeId) {
        return states.getOrDefault(nodeId, NodeState.LOCKED);
    }

    /** Прогресс текущего исследования, тик (0 при отсутствии). */
    public int getProgressTicks(String nodeId) {
        return progressTicks.getOrDefault(nodeId, 0);
    }

    /** Полная длительность текущего исследования, тик (0 при отсутствии). */
    public int getTotalTicks(String nodeId) {
        return totalTicks.getOrDefault(nodeId, 0);
    }

    /** Доля завершения исследования 0..1. */
    public float getProgressFraction(String nodeId) {
        int total = getTotalTicks(nodeId);
        return total <= 0 ? 0f : Math.min(1f, (float) getProgressTicks(nodeId) / total);
    }

    public boolean isUnlocked(String nodeId) {
        return getState(nodeId) == NodeState.UNLOCKED;
    }

    public void setState(String nodeId, NodeState state) {
        states.put(nodeId, state);
        if (state != NodeState.IN_PROGRESS) {
            progressTicks.remove(nodeId);
            totalTicks.remove(nodeId);
        }
    }

    /** Начинает исследование (сбрасывает прогресс). */
    public void start(String nodeId, int totalTicks) {
        states.put(nodeId, NodeState.IN_PROGRESS);
        progressTicks.put(nodeId, 0);
        this.totalTicks.put(nodeId, Math.max(1, totalTicks));
    }

    /**
     * Продвигает исследование на {@code ticks} тиков.
     *
     * @return {@code true}, если узел только что завершён
     */
    public boolean advance(String nodeId, int ticks) {
        if (getState(nodeId) != NodeState.IN_PROGRESS) return false;
        int progress = progressTicks.getOrDefault(nodeId, 0) + ticks;
        int total = totalTicks.getOrDefault(nodeId, 1);
        if (progress >= total) {
            setState(nodeId, NodeState.UNLOCKED);
            return true;
        }
        progressTicks.put(nodeId, progress);
        return false;
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag nodes = new CompoundTag();
        for (Map.Entry<String, NodeState> e : states.entrySet()) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putInt("state", e.getValue().ordinal());
            nodeTag.putInt("progress", progressTicks.getOrDefault(e.getKey(), 0));
            nodeTag.putInt("total", totalTicks.getOrDefault(e.getKey(), 0));
            nodes.put(e.getKey(), nodeTag);
        }
        tag.put("nodes", nodes);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        states.clear();
        progressTicks.clear();
        totalTicks.clear();
        CompoundTag nodes = tag.getCompound("nodes");
        for (String key : nodes.getAllKeys()) {
            CompoundTag nodeTag = nodes.getCompound(key);
            int stateOrdinal = nodeTag.getInt("state");
            NodeState[] values = NodeState.values();
            NodeState state = stateOrdinal >= 0 && stateOrdinal < values.length ? values[stateOrdinal] : NodeState.LOCKED;
            states.put(key, state);
            if (state == NodeState.IN_PROGRESS) {
                progressTicks.put(key, nodeTag.getInt("progress"));
                totalTicks.put(key, nodeTag.getInt("total"));
            }
        }
    }

    /** Копирует состояние из другого прогресса (для синхронизации на клиенте). */
    public void copyFrom(ResearchProgress other) {
        states.clear();
        progressTicks.clear();
        totalTicks.clear();
        states.putAll(other.states);
        progressTicks.putAll(other.progressTicks);
        totalTicks.putAll(other.totalTicks);
    }
}
