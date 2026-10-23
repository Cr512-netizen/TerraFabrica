package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Сверлильный станок: отверстия в металле и дереве. RPM 200–1000. */
public class DrillPressBlockEntity extends ToolMachineBlockEntity {

    public DrillPressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.DRILL_PRESS.get(), 200, 1_000, 3_000);
    }

    @Override
    protected String machineId() {
        return "drill_press";
    }
}
