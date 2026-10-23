package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Шлифовальный станок: финишная обработка, повышает качество деталей. RPM 500–2000. */
public class GrindingMachineBlockEntity extends ToolMachineBlockEntity {

    public GrindingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.GRINDING_MACHINE.get(), 500, 2_000, 6_000);
    }

    @Override
    protected String machineId() {
        return "grinding_machine";
    }
}
