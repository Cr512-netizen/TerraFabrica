package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Тяжёлый прокатный стан (5×3×3): рельсы, балки, листы. Электрический или
 * механический привод; валки изнашиваются — деталь «валок» расходуется.
 */
public class HeavyRollingMillBlockEntity extends ToolMachineBlockEntity {

    public HeavyRollingMillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.HEAVY_ROLLING_MILL.get(), 50, 200, 30_000);
    }

    @Override
    protected String machineId() {
        return "heavy_rolling_mill";
    }

    @Override
    protected boolean conditionsMet(MachineRecipe recipe) {
        // Заготовка должна быть нагрета (900 °C), как и в лёгком прокатном стане.
        if (heat.getTemperature() < 1173.15) {
            status = STATUS_COLD;
            return false;
        }
        return super.conditionsMet(recipe);
    }
}
