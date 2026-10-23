package com.terrafabrica.core.research;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.TFAttachments;
import com.terrafabrica.core.network.ResearchSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Серверная логика системы исследований (авторитетна в мультиплеере).
 *
 * <p>Поток состояний: LOCKED → AVAILABLE (когда все предшественники открыты) →
 * IN_PROGRESS (игрок заплатил ресурсы) → UNLOCKED (по истечении времени).
 * Ресурсы изымаются при старте; прогресс сохраняется в attachment игрока и
 * переживает перезаход.
 */
public final class ResearchManager {

    private ResearchManager() {}

    // ------------------------------------------------------------------
    // Состояния и доступность
    // ------------------------------------------------------------------

    /** Все ли предшественники узла открыты. */
    public static boolean prerequisitesUnlocked(ResearchProgress progress, ResearchNode node) {
        for (String prereq : node.prerequisites()) {
            if (!progress.isUnlocked(prereq)) return false;
        }
        return true;
    }

    /**
     * Пересчитывает доступность узлов (LOCKED ↔ AVAILABLE) по открытым
     * предшественникам. Вызывается после завершения исследования и при входе.
     */
    public static void recomputeAvailability(Player player) {
        ResearchProgress progress = player.getData(TFAttachments.RESEARCH_PROGRESS);
        for (ResearchNode node : ResearchTree.all()) {
            ResearchProgress.NodeState state = progress.getState(node.id());
            if (state == ResearchProgress.NodeState.UNLOCKED || state == ResearchProgress.NodeState.IN_PROGRESS) {
                continue;
            }
            boolean available = prerequisitesUnlocked(progress, node);
            if (available && state == ResearchProgress.NodeState.LOCKED) {
                progress.setState(node.id(), ResearchProgress.NodeState.AVAILABLE);
            } else if (!available && state == ResearchProgress.NodeState.AVAILABLE) {
                progress.setState(node.id(), ResearchProgress.NodeState.LOCKED);
            }
        }
    }

    public static boolean canStart(Player player, ResearchNode node) {
        ResearchProgress progress = player.getData(TFAttachments.RESEARCH_PROGRESS);
        return progress.getState(node.id()) == ResearchProgress.NodeState.AVAILABLE
                && prerequisitesUnlocked(progress, node);
    }

    // ------------------------------------------------------------------
    // Старт исследования
    // ------------------------------------------------------------------

    /**
     * Начинает исследование: проверяет доступность, изымает ресурсы из
     * инвентаря и переводит узел в IN_PROGRESS. Синхронизирует с клиентом.
     *
     * @return {@code true}, если исследование начато
     */
    public static boolean startResearch(ServerPlayer player, String nodeId) {
        ResearchNode node = ResearchTree.get(nodeId);
        if (node == null) return false;

        ResearchProgress progress = player.getData(TFAttachments.RESEARCH_PROGRESS);
        if (progress.getState(nodeId) != ResearchProgress.NodeState.AVAILABLE
                || !prerequisitesUnlocked(progress, node)) {
            return false;
        }

        // Оплата ресурсами (только из основного инвентаря).
        if (!player.getAbilities().instabuild && !consumeCosts(player, node.costs())) {
            player.displayClientMessage(
                    Component.translatable("message.terrafabrica.research_missing_resources",
                            Component.translatable(node.langKey())).withStyle(ChatFormatting.YELLOW),
                    false);
            return false;
        }

        int totalTicks = (int) Math.max(1, node.timeTicks() * TFServerConfig.RESEARCH_SPEED_MULTIPLIER.get());
        progress.start(nodeId, totalTicks);
        recomputeAvailability(player);
        sync(player);
        player.displayClientMessage(
                Component.translatable("message.terrafabrica.research_started",
                        Component.translatable(node.langKey())).withStyle(ChatFormatting.GREEN),
                false);
        return true;
    }

    /** Изымает ресурсы из инвентаря игрока; {@code false} при нехватке. */
    private static boolean consumeCosts(ServerPlayer player, List<ResearchNode.ItemCost> costs) {
        // Сначала проверяем наличие.
        for (ResearchNode.ItemCost cost : costs) {
            if (countItems(player, cost.item().asItem()) < cost.count()) {
                return false;
            }
        }
        // Затем изымаем.
        for (ResearchNode.ItemCost cost : costs) {
            removeItems(player, cost.item().asItem(), cost.count());
        }
        return true;
    }

    private static int countItems(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void removeItems(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    // ------------------------------------------------------------------
    // Тик прогресса
    // ------------------------------------------------------------------

    /**
     * Продвигает активные исследования на 1 тик. Вызывается серверным тиком
     * для каждого онлайн-игрока. При завершении узла пересчитывает доступность
     * потомков и синхронизирует прогресс.
     */
    public static void tickServer(ServerPlayer player) {
        ResearchProgress progress = player.getData(TFAttachments.RESEARCH_PROGRESS);
        boolean changed = false;
        String completed = null;

        for (ResearchNode node : ResearchTree.all()) {
            if (progress.getState(node.id()) == ResearchProgress.NodeState.IN_PROGRESS) {
                if (progress.advance(node.id(), 1)) {
                    changed = true;
                    completed = node.id();
                } else {
                    changed = true; // прогресс изменился — нужно синхронизировать бар
                }
            }
        }

        if (completed != null) {
            recomputeAvailability(player);
            ResearchNode node = ResearchTree.get(completed);
            player.displayClientMessage(
                    Component.translatable("message.terrafabrica.research_completed",
                            Component.translatable(node.langKey())).withStyle(ChatFormatting.GOLD),
                    false);
        }
        if (changed) {
            sync(player);
        }
    }

    /** Отправляет игроку актуальный снимок прогресса. */
    public static void sync(ServerPlayer player) {
        player.connection.send(new ResearchSyncPayload(
                player.getData(TFAttachments.RESEARCH_PROGRESS).serializeNBT(player.level().registryAccess())));
    }
}
