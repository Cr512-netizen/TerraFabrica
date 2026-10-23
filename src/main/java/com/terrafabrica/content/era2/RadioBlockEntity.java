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
 * Радио (2×2×3): ламповый передатчик с антенной. Изменение редстоун-сигнала
 * ретранслируется всем радиостанциям в радиусе 10 000 блоков (упрощение:
 * без несущей частоты и модуляции — см. docs/DESIGN_DECISIONS.md).
 */
public class RadioBlockEntity extends AbstractMachineBlockEntity {

    private int lastSignal;
    private int cooldown;

    public RadioBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 2_000, 500, 0, 0, 4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        int signal = level.getBestNeighborSignal(worldPosition);
        if (signal != lastSignal && cooldown <= 0) {
            lastSignal = signal;
            cooldown = 10;
            for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-250, -64, -250),
                    worldPosition.offset(250, 64, 250))) {
                if (pos.equals(worldPosition)) continue;
                if (level.getBlockEntity(pos) instanceof RadioBlockEntity other) {
                    other.lastSignal = signal;
                    other.cooldown = 10;
                }
            }
        }
        if (cooldown > 0) cooldown--;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.radio");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
