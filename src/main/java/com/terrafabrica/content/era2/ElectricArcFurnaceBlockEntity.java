package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
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
 * Электродуговая печь (мультиблок 4×4×4): 100 кВт, до 2000 °C. Точное
 * легирование хромом, никелем, молибденом, ванадием, титаном — по весу
 * (весовые дозаторы); нагрев — электричеством (100 кВт → тепло).
 */
public class ElectricArcFurnaceBlockEntity extends MultiblockMachineBlockEntity {

    /** Электрическая мощность нагрева, Вт. */
    public static final long HEATING_POWER_W = 100_000;

    public ElectricArcFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                1_000_000, 200_000, 0,
                120_000, 2_273.15,
                0, 0,
                8_000, 8_000,
                MultiblockHelper.solidBox(4, 4, 4, TFBlocks.REFRACTORY_BRICK.get()));
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
        // Электрический нагрев: 100 кВт → 5 кДж/тик.
        long draw = energy.extractEnergy(HEATING_POWER_W / 20L, false);
        if (draw > 0) {
            heat.addHeat(draw * 0.9, false); // КПД дуги ~90%
        }
        boolean active = tryWork(0, 0);
        updateBlockState(active);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.ELECTRIC_ARC_FURNACE.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.electric_arc_furnace");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
