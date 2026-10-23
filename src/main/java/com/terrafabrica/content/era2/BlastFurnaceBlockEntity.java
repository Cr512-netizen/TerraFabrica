package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockFurnaceBlockEntity;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Домна на коксе (мультиблок 5×5×10): производительность 10 т чугуна в сутки
 * (игровой эквивалент — цикл 60 с на 2 чугуна при полном шихтовании).
 *
 * <p>Горячее дутьё 800 °C: рецепты с {@code min_temperature ≥ 1073 K} дают
 * +50% выхода (4 концентрата + 2 кокса + 1 известняк → 3 чугуна + 1 шлак),
 * что эквивалентно экономии кокса ~30% (см. docs/BALANCE.md).
 */
public class BlastFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public BlastFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1773.15, 400_000, 15_000,
                MultiblockHelper.solidBox(5, 10, 5, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "blast_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.BLAST_FURNACE.get();
    }
}
