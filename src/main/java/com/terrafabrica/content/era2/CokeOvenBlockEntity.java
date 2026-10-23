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
 * Коксовая печь (мультиблок 3×3×3): уголь → кокс при 1000 °C без доступа воздуха.
 * Побочные продукты — смола (item) и светильный газ (fluid). Их нельзя
 * выбрасывать: при переполнении выходов печь останавливается; газ сжигается
 * в горелке (рецепт в печах) или перерабатывается в химикаты.
 */
public class CokeOvenBlockEntity extends MultiblockFurnaceBlockEntity {

    public CokeOvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1273.15, 90_000, 10_000,
                MultiblockHelper.solidBox(3, 3, 3, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "coke_oven";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.COKE_OVEN.get();
    }
}
