package com.terrafabrica.content.shared.machine;

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

import java.util.List;

/**
 * Мультиблочная топливная печь: структура из обшивки + топка + рецепты с
 * требованием температуры. (Композиция {@link MultiblockMachineBlockEntity} и
 * {@link BurnerLogic} — в Java нет множественного наследования.)
 */
public abstract class MultiblockFurnaceBlockEntity extends MultiblockMachineBlockEntity {

    public static final int FUEL_SLOT = 3;
    protected final BurnerLogic burner;

    protected MultiblockFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                           double maxTempK, double heatCapacity, double burnPower,
                                           List<MultiblockHelper.Part> structure) {
        super(type, pos, state, 0, 0, 0, heatCapacity, maxTempK, 0, 0,
                8_000, 8_000, structure);
        this.burner = new BurnerLogic(heat, burnPower);
    }

    @Override
    protected void tickServer() {
        if (--checkCooldown <= 0) {
            checkCooldown = STRUCTURE_CHECK_INTERVAL;
            formed = checkStructure();
        }
        if (!formed) {
            status = STATUS_NO_STRUCTURE;
            progress = 0;
            currentRecipe = null;
            updateBlockState(false);
            return;
        }
        heat.coolToAmbient(false);
        burner.tick(inventory, FUEL_SLOT);
        boolean active = tryWork(0, 0);
        updateBlockState(active);
    }

    public boolean isBurning() {
        return burner.isBurning();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + machineId());
    }

    protected abstract String machineId();

    @Override
    public abstract RecipeType<MachineRecipe> recipeType();

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
