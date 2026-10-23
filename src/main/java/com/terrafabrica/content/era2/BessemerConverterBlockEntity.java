package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Бессемеровский конвертер (мультиблок 3×3×4): продувка кислородом, чугун →
 * сталь за 15–20 минут (21600 тик), выход 90%. Экзотермический процесс —
 * топливо не нужно. Шлак необходимо удалять (выходной слот), иначе
 * конвертер блокируется.
 */
public class BessemerConverterBlockEntity extends MultiblockMachineBlockEntity {

    public BessemerConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                150_000, 2_273.15,
                0, 0,
                8_000, 8_000,
                MultiblockHelper.solidBox(3, 4, 3, TFBlocks.REACTOR_CASING.get()));
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.BESSEMER_CONVERTER.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.bessemer_converter");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
