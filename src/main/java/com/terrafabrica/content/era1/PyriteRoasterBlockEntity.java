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
 * Печь обжига пирита (мультиблок 3×3×3): 800 °C. Пирит + воздух → SO₂
 * (газ улавливается в свинцовую камеру — выброс в атмосферу портит машины
 * рядом, поэтому SO₂ нужно отводить по трубам).
 */
public class PyriteRoasterBlockEntity extends MultiblockFurnaceBlockEntity {

    public PyriteRoasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1073.15, 50_000, 8_000,
                MultiblockHelper.solidBox(3, 3, 3, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "pyrite_roaster";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.PYRITE_ROASTER.get();
    }
}
