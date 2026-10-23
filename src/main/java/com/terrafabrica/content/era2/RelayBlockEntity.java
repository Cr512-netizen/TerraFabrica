package com.terrafabrica.content.era2;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Реле: редстоун-управляемый коммутатор энергии. При сигнале редстоуна
 * пропускает энергию между задней и передней стороной (мостик между сетями).
 */
public class RelayBlockEntity extends AbstractMachineBlockEntity {

    public RelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 200_000, 100_000, 100_000, 1_000, 400, 0, 0, 4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        boolean powered = level.getBestNeighborSignal(worldPosition) > 0;
        updateBlockState(powered);
        if (!powered) {
            status = STATUS_IDLE;
            return;
        }
        Direction facing = getBlockState().getValue(com.terrafabrica.content.shared.machine.MachineBlock.FACING);
        BlockEntity back = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        BlockEntity front = level.getBlockEntity(worldPosition.relative(facing));
        if (back instanceof AbstractMachineBlockEntity from
                && front instanceof AbstractMachineBlockEntity to) {
            long move = Math.min(from.getStoredEnergy(), 50_000);
            long delivered = from.extractEnergy(move, false);
            to.receiveEnergy(delivered, false);
        }
        status = STATUS_WORKING;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.relay");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
