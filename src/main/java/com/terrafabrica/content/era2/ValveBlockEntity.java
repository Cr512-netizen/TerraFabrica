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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Клапан: исполнительный механизм флюидных систем. При сигнале редстоуна
 * перекачивает флюид из бака сзади в бак спереди (до 1000 мБ/тик).
 */
public class ValveBlockEntity extends AbstractMachineBlockEntity {

    public ValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 1_000, 400, 0, 0, 4, 4, 0, 0);
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
        IFluidHandler back = level.getCapability(Capabilities.FluidHandler.BLOCK,
                worldPosition.relative(facing.getOpposite()), facing);
        IFluidHandler front = level.getCapability(Capabilities.FluidHandler.BLOCK,
                worldPosition.relative(facing), facing.getOpposite());
        if (back != null && front != null) {
            var drained = back.drain(1_000, IFluidHandler.FluidAction.SIMULATE);
            if (!drained.isEmpty()) {
                int accepted = front.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    back.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                    front.fill(drained.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
        status = STATUS_WORKING;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.valve");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
