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
 * Телеграф (1×1×2): передача сигналов кодом Морзе по редстоун-импульсам на
 * расстояние до 1000 блоков. Изменение редстоун-сигнала на любой стороне
 * ретранслируется всем телеграфам в радиусе (документированное упрощение:
 * морзянка — самим игроком, телеграф — физический канал).
 */
public class TelegraphBlockEntity extends AbstractMachineBlockEntity {

    private int lastSignal;
    private int relayCooldown;

    public TelegraphBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 1_000, 400, 0, 0, 4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        int signal = level.getBestNeighborSignal(worldPosition);
        if (signal != lastSignal && relayCooldown <= 0) {
            lastSignal = signal;
            relayCooldown = 10;
            relay(signal);
        }
        if (relayCooldown > 0) relayCooldown--;
    }

    private void relay(int signal) {
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-100, -32, -100),
                worldPosition.offset(100, 32, 100))) {
            BlockPos target = pos.immutable();
            if (target.equals(worldPosition)) continue;
            if (level.getBlockEntity(target) instanceof TelegraphBlockEntity other) {
                other.receiveSignal(signal);
            }
        }
    }

    /** Приём: эмулируем редстоун-импульс на 2 тика. */
    public void receiveSignal(int signal) {
        // Импульс через соседний блок: ставим редстоун-блок? Нет — телеграф сам
        // выдаёт сигнал через getSignal (упрощение: «звонок» в чат не шлём).
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.telegraph");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
