package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Прокатный стан: листы, прутки, рельсы. RPM 50–200, заготовка должна быть нагрета до 900 °C. */
public class RollingMillBlockEntity extends ToolMachineBlockEntity {

    /** Требуемая температура заготовки, K (900 °C). */
    public static final double REQUIRED_TEMPERATURE_K = 1173.15;

    public RollingMillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.ROLLING_MILL.get(), 50, 200, 8_000);
    }

    @Override
    protected String machineId() {
        return "rolling_mill";
    }

    @Override
    protected boolean conditionsMet(MachineRecipe recipe) {
        if (heat.getTemperature() < REQUIRED_TEMPERATURE_K) {
            status = STATUS_COLD;
            return false;
        }
        return super.conditionsMet(recipe);
    }
}
