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
import org.jetbrains.annotations.Nullable;

/**
 * Программируемый логический контроллер на лампах: цепочка
 * «датчик → контроллер → исполнительный механизм».
 *
 * <p>Режим логики переключается ПКМ+Shift: AND / OR / XOR / NOT / THRESHOLD.
 * Входы — редстоун с 4 сторон; выход — сторона facing.
 */
public class PlcBlockEntity extends AbstractMachineBlockEntity {

    /** Режимы логики. */
    public enum Logic {
        AND, OR, XOR, NOT, THRESHOLD
    }

    private Logic logic = Logic.AND;
    private int outputSignal;

    public PlcBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 1_000, 400, 0, 0, 4, 4, 0, 0);
    }

    public Logic logic() {
        return logic;
    }

    public void cycleLogic() {
        logic = Logic.values()[(logic.ordinal() + 1) % Logic.values().length];
        setChanged();
    }

    @Override
    protected void tickServer() {
        int a = level.getSignal(worldPosition.north(), Direction.NORTH);
        int b = level.getSignal(worldPosition.south(), Direction.SOUTH);
        int c = level.getSignal(worldPosition.east(), Direction.EAST);
        int d = level.getSignal(worldPosition.west(), Direction.WEST);
        boolean anyA = a > 0, anyB = b > 0, anyC = c > 0, anyD = d > 0;
        int result = switch (logic) {
            case AND -> (anyA && anyB && anyC && anyD) ? 15 : 0;
            case OR -> (anyA || anyB || anyC || anyD) ? 15 : 0;
            case XOR -> ((anyA ^ anyB) || (anyC ^ anyD)) ? 15 : 0;
            case NOT -> anyA ? 0 : 15;
            case THRESHOLD -> (a + b + c + d) >= 30 ? 15 : 0;
        };
        if (result != outputSignal) {
            outputSignal = result;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        updateBlockState(result > 0);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.plc");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
