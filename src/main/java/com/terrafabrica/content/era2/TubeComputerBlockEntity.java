package com.terrafabrica.content.era2;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Ламповый компьютер (4×4×4): 100–500 кВт, тысячи операций в секунду, память
 * на магнитных сердечниках до 100 КБ (в игровом эквиваленте — 4 регистра по
 * 15 уровней). Выполняет микропрограмму: счётчик тиков, сравнение с порогами,
 * 4 выхода редстоуна для управления автоматикой.
 */
public class TubeComputerBlockEntity extends AbstractMachineBlockEntity {

    /** Регистры R0..R3. */
    private final int[] registers = new int[4];
    /** Пороги сравнения для выходов. */
    private final int[] thresholds = {8, 8, 8, 8};

    public TubeComputerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 2_000_000, 100_000, 0, 10_000, 700, 0, 0, 4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        // Питание: 100–500 кВт. Без энергии компьютер выключен.
        long needed = 5_000; // 100 кВт → Дж/тик
        if (energy.extractEnergy(needed, false) < needed) {
            status = STATUS_NO_POWER;
            updateBlockState(false);
            return;
        }
        status = STATUS_WORKING;
        updateBlockState(true);

        // Микропрограмма: R0 — счётчик; входы редстоуна с 4 сторон — в R1..R3.
        registers[0] = (registers[0] + 1) & 0x7FFF;
        registers[1] = level.getSignal(worldPosition.south(), net.minecraft.core.Direction.SOUTH);
        registers[2] = level.getSignal(worldPosition.west(), net.minecraft.core.Direction.WEST);
        registers[3] = level.getSignal(worldPosition.north(), net.minecraft.core.Direction.NORTH);
        // Выходы: 15, если регистр превышает порог.
        for (int i = 0; i < 4; i++) {
            int out = registers[i] >= thresholds[i] ? 15 : 0;
            level.setBlock(worldPosition.relative(net.minecraft.core.Direction.from2DDataValue(i)),
                    getBlockState().getBlock().defaultBlockState(), 3);
        }
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.tube_computer");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
