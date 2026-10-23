package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Токарный станок: валы, оси, болты, гайки. RPM 100–500, точность 0.5 мм. */
public class LatheBlockEntity extends ToolMachineBlockEntity {

    public LatheBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.LATHE.get(), 100, 500, 2_000);
    }

    @Override
    protected String machineId() {
        return "lathe";
    }
}
