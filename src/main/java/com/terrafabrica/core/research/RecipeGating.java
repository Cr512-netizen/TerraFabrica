package com.terrafabrica.core.research;

import com.terrafabrica.content.TFAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Единая точка проверки блокировки рецептов исследованиями.
 *
 * <p>Правила:
 * <ul>
 *   <li>рецепт без узла ({@code requiredNode() == null}) доступен всегда;</li>
 *   <li>креативный режим обходит блокировку (для удобства тестирования);</li>
 *   <li>проверка выполняется на сервере по attachment игрока — в мультиплеере
 *       у каждого игрока свой прогресс.</li>
 * </ul>
 */
public final class RecipeGating {

    private RecipeGating() {}

    /** Минимальный интервал сообщений «требуется исследование», тик (5 с). */
    private static final int MESSAGE_COOLDOWN_TICKS = 100;
    private static final Map<UUID, Long> LAST_MESSAGE_TICK = new HashMap<>();

    /**
     * Открыт ли узел для игрока.
     *
     * @param player игрок (может быть клиентским — attachment синхронизирован)
     * @param nodeId id узла или {@code null} (нет требования)
     */
    public static boolean isUnlocked(Player player, @Nullable String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) return true;
        // Нет игрока — нечем проверить прогресс (машина без владельца,
        // автоматизация). Рецепт доступен: прогрессию держит крафт самой
        // машины, а не её внутренние рецепты (см. docs/decisions.md).
        if (player == null) return true;
        if (player.getAbilities().instabuild) return true; // креатив обходит гейтинг
        ResearchProgress progress = player.getData(TFAttachments.RESEARCH_PROGRESS);
        return progress.isUnlocked(nodeId);
    }

    /** Требуемый узел рецепта, если рецепт гейтится. */
    @Nullable
    public static String requiredNodeOf(Recipe<?> recipe) {
        return recipe instanceof IResearchGatedRecipe gated ? gated.requiredNode() : null;
    }

    /** Доступен ли рецепт игроку с учётом исследований. */
    public static boolean isRecipeUnlocked(Player player, Recipe<?> recipe) {
        return isUnlocked(player, requiredNodeOf(recipe));
    }

    /**
     * Отправляет игроку сообщение о заблокированном рецепте (с кулдауном,
     * чтобы не спамить при удержании крафта).
     */
    public static void sendLockedMessage(Player player, @Nullable String nodeId) {
        if (nodeId == null || player.level().isClientSide) return;
        long now = player.level().getGameTime();
        Long last = LAST_MESSAGE_TICK.get(player.getUUID());
        if (last != null && now - last < MESSAGE_COOLDOWN_TICKS) return;
        LAST_MESSAGE_TICK.put(player.getUUID(), now);

        ResearchNode node = ResearchTree.get(nodeId);
        Component name = node != null
                ? Component.translatable(node.langKey())
                : Component.literal(nodeId);
        player.displayClientMessage(
                Component.translatable("message.terrafabrica.recipe_locked", name)
                        .withStyle(ChatFormatting.RED),
                true);
    }

    /** Удобная перегрузка для серверного игрока. */
    public static void sendLockedMessage(ServerPlayer player, @Nullable String nodeId) {
        sendLockedMessage((Player) player, nodeId);
    }
}
