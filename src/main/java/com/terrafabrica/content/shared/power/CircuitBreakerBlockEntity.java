package com.terrafabrica.content.shared.power;

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
 * Автоматический выключатель: при коротком замыкании/перегрузке соседнего
 * кабеля срабатывает (не сгорает вся сеть): выдаёт сигнал редстоуна и
 * блокирует передачу через себя, пока не будет переключён вручную (ПКМ).
 */
public class CircuitBreakerBlockEntity extends AbstractMachineBlockEntity {

    private boolean tripped;
    private int tripSignalTicks;

    public CircuitBreakerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                1_000, 400,
                0, 0,
                4, 4, 0, 0);
    }

    /** Вызывается кабелем при перегрузке. */
    public void onTrip() {
        tripped = true;
        tripSignalTicks = 100;
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    public boolean isTripped() {
        return tripped;
    }

    /** Ручной сброс (ПКМ). */
    public void reset() {
        tripped = false;
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Override
    protected void tickServer() {
        if (tripSignalTicks > 0) {
            tripSignalTicks--;
            if (tripSignalTicks == 0) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
        // Буфер не хранит энергию: выключатель — коммутационный элемент.
        status = tripped ? STATUS_NO_POWER : STATUS_IDLE;
    }

    /** Сигнал редстоуна при срабатывании. */
    public int getSignal() {
        return tripped ? 15 : 0;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.circuit_breaker");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
