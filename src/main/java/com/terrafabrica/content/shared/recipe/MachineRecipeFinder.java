package com.terrafabrica.content.shared.recipe;

import com.terrafabrica.core.research.RecipeGating;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Помощник поиска машинных рецептов с учётом гейтинга исследованиями.
 *
 * <p>Машина ищет первый подходящий рецепт из датапака, для которого игрок
 * (открывший GUI/владелец машины) открыл требуемый узел. Негейтящиеся рецепты
 * доступны всегда.
 */
public final class MachineRecipeFinder {

    private MachineRecipeFinder() {}

    /**
     * Ищет подходящий рецепт.
     *
     * @param level  уровень (реестр рецептов)
     * @param type   тип рецептов машины
     * @param input  предметный вход
     * @param player игрок для проверки исследований (может быть {@code null} —
     *               тогда учитываются только негейтящиеся рецепты)
     */
    public static Optional<RecipeHolder<MachineRecipe>> find(Level level, RecipeType<MachineRecipe> type,
                                                             MachineRecipeInput input, @Nullable Player player) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<MachineRecipe>> candidates = manager.getAllRecipesFor(type);
        for (RecipeHolder<MachineRecipe> holder : candidates) {
            MachineRecipe recipe = holder.value();
            if (!recipe.matches(input, level)) continue;
            // Гейтинг проверяется только при известном игроке (владелец машины
            // или последний открывший GUI): иначе автоматизация ломалась бы
            // при выходе игрока из мира.
            if (player != null && !RecipeGating.isUnlocked(player, recipe.requiredNode())) continue;
            return Optional.of(holder);
        }
        return Optional.empty();
    }

    /** Есть ли для входа хоть какой-то рецепт, заблокированный исследованием. */
    public static boolean hasLockedCandidate(Level level, RecipeType<MachineRecipe> type,
                                             MachineRecipeInput input, Player player) {
        RecipeManager manager = level.getRecipeManager();
        for (RecipeHolder<MachineRecipe> holder : manager.getAllRecipesFor(type)) {
            MachineRecipe recipe = holder.value();
            if (player != null && recipe.matches(input, level)
                    && !RecipeGating.isUnlocked(player, recipe.requiredNode())) {
                return true;
            }
        }
        return false;
    }
}
