package com.terrafabrica.mixin;

import com.terrafabrica.core.research.IResearchGatedRecipe;
import com.terrafabrica.core.research.RecipeGating;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * ЕДИНСТВЕННЫЙ миксин мода (документированное «крайнее средство»).
 *
 * <p>Зачем: ванильный поиск рецепта крафта ({@code RecipeManager#getRecipeFor})
 * не передаёт игрока в {@code matches()}, поэтому проверить персональный
 * прогресс исследований внутри рецепта невозможно. Этот миксин перехватывает
 * {@code CraftingMenu#slotChangedCraftingGrid} — единую точку, через которую
 * идут и верстак 3×3, и сетка инвентаря 2×2, — и блокирует результат для
 * рецептов, требующих неоткрытый узел исследования.
 *
 * <p>Серверная логика авторитетна: на клиенте миксин тоже выполняется, но
 * использует синхронизированный прогресс только для скрытия предпросмотра.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    // Сигнатура обязана совпадать с целью: в 1.21.1 это
    // (AbstractContainerMenu, Level, Player, CraftingContainer, ResultContainer,
    //  RecipeHolder<CraftingRecipe>). Последний параметр — уже найденный ванилью
    // рецепт, поэтому используем его вместо повторного поиска.
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void terrafabrica$gateResearchRecipes(AbstractContainerMenu menu, Level level, Player player,
                                                        CraftingContainer container, ResultContainer resultContainer,
                                                        RecipeHolder<CraftingRecipe> recipeHolder,
                                                        CallbackInfo ci) {
        RecipeHolder<CraftingRecipe> holder = recipeHolder;
        if (holder == null) {
            // Ваниль передаёт null, если рецепт не найден; на всякий случай
            // повторяем поиск, чтобы гейтинг не терялся.
            Optional<RecipeHolder<CraftingRecipe>> found = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, container.asCraftInput(), level);
            if (found.isEmpty()) {
                return;
            }
            holder = found.get();
        }
        CraftingRecipe recipe = holder.value();
        if (recipe instanceof IResearchGatedRecipe gated && !RecipeGating.isUnlocked(player, gated.requiredNode())) {
            // Рецепт заблокирован: очищаем результат и прерываем ванильную логику.
            resultContainer.setItem(0, ItemStack.EMPTY);
            if (!level.isClientSide) {
                RecipeGating.sendLockedMessage(player, gated.requiredNode());
            }
            ci.cancel();
        }
    }
}
