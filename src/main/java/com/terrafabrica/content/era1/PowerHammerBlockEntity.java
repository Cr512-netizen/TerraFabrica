package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.ToolMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Механический молот: ковка крупных деталей, увеличивает прочность. RPM 50–100. */
public class PowerHammerBlockEntity extends ToolMachineBlockEntity {

    public PowerHammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, TFRecipeTypes.POWER_HAMMER.get(), 50, 100, 10_000);
    }

    @Override
    protected String machineId() {
        return "power_hammer";
    }
}
