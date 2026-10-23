package com.terrafabrica.content.shared.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Вход машинного рецепта: упорядоченный список предметных слотов.
 * Порядок слотов соответствует порядку {@code ingredients} рецепта.
 */
public record MachineRecipeInput(ItemStack[] items) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < items.length ? items[index] : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return items.length;
    }
}
