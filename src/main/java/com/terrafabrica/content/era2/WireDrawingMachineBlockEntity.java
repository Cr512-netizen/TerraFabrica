package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Волочильный стан: проволока из меди, алюминия, стали. RPM 100–500, требует смазку. */
public class WireDrawingMachineBlockEntity extends ToolMachineBlockEntity {

    public static final int LUBRICANT_PER_CYCLE_MB = 30;

    public WireDrawingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.WIRE_DRAWING_MACHINE.get(), 100, 500, 4_000);
    }

    @Override
    protected String machineId() {
        return "wire_drawing_machine";
    }

    @Override
    protected boolean tryWork(double availablePower, double availableRpm) {
        if (inputTank.getFluidAmount() < LUBRICANT_PER_CYCLE_MB) {
            status = STATUS_NO_FLUID;
            progress = 0;
            currentRecipe = null;
            return false;
        }
        boolean worked = super.tryWork(availablePower, availableRpm);
        if (worked && progress >= maxProgress) {
            inputTank.drain(LUBRICANT_PER_CYCLE_MB,
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        }
        return worked;
    }
}
