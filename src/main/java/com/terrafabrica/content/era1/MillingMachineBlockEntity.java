package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Фрезерный станок: плоскости, пазы. RPM 300–1500, требует смазку (масло в баке). */
public class MillingMachineBlockEntity extends ToolMachineBlockEntity {

    /** Расход масла на цикл, мБ. */
    public static final int LUBRICANT_PER_CYCLE_MB = 50;

    public MillingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.MILLING_MACHINE.get(), 300, 1_500, 5_000);
    }

    @Override
    protected String machineId() {
        return "milling_machine";
    }

    @Override
    protected boolean tryWork(double availablePower, double availableRpm) {
        if (inputTank.getFluidAmount() < LUBRICANT_PER_CYCLE_MB) {
            status = STATUS_NO_FLUID; // нет смазки
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
