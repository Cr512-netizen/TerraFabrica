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
 * Тигельная печь (мультиблок 2×2×2): до 1600 °C. Стекло, бронза, латунь,
 * специальные стёкла. Бронза: 9 меди + 1 олово → 10 бронзы.
 */
public class CrucibleFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public CrucibleFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1873.15, 70_000, 10_000,
                MultiblockHelper.solidBox(2, 2, 2, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "crucible_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.CRUCIBLE_FURNACE.get();
    }
}
