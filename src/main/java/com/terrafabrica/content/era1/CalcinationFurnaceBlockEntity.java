package com.terrafabrica.content.era1;

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
 * Печь прокалки (мультиблок 2×2×3): 200 °C. NaHCO₃ → Na₂CO₃ + CO₂ (CO₂
 * возвращается в процесс Сольве — замкнутый цикл).
 */
public class CalcinationFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public CalcinationFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 473.15, 20_000, 4_000,
                MultiblockHelper.solidBox(2, 3, 2, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "calcination_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.CALCINATION_FURNACE.get();
    }
}
