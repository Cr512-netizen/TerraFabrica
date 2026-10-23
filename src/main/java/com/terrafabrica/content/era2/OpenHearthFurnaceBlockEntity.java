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
 * Мартеновская печь (мультиблок 5×5×5): 1700 °C, КПД 50%, переплав лома с рудой
 * (рецепты со стальным ломом + концентратами дают сталь дёшево, но медленно).
 */
public class OpenHearthFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public OpenHearthFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1973.15, 350_000, 15_000,
                MultiblockHelper.solidBox(5, 5, 5, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "open_hearth_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.OPEN_HEARTH_FURNACE.get();
    }
}
