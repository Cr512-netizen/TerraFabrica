package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.content.shared.power.IVoltageSource;
import com.terrafabrica.core.util.Units;
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
 * Электрогенератор (мультиблок 3×3×3): синхронный трёхфазный, 50 Гц, 10 кВ,
 * КПД 95–98%. Требует вращения и возбуждения (1% мощности тратится на
 * возбудитель). Выход — электричество 10 кВ в сеть кабелей.
 */
public class ElectricGeneratorBlockEntity extends MultiblockMachineBlockEntity implements IVoltageSource {

    public ElectricGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                10_000_000, 0, 5_000_000,      // буфер 10 МДж, отдача 5 МДж/тик (100 МВт)
                40_000, 700,
                3_000, 200_000,
                0, 0,
                MultiblockHelper.solidBox(3, 3, 3, TFBlocks.IRON_CASING.get()));
    }

    @Override
    public double nominalVoltage() {
        return 10_000.0; // 10 кВ
    }

    @Override
    protected void tickServer() {
        if (--checkCooldown <= 0) {
            checkCooldown = STRUCTURE_CHECK_INTERVAL;
            formed = checkStructure();
        }
        if (!formed) {
            status = STATUS_NO_STRUCTURE;
            updateBlockState(false);
            return;
        }
        // Механика → электричество: КПД 95–98%, минус возбуждение 1%.
        double mechanicalPower = rotation.getPower();
        double rpm = rotation.getRPM();
        rotation.reset();
        double efficiency = rpm >= 2_900 ? 0.98 : 0.95;
        double produced = mechanicalPower * efficiency * 0.99;
        energy.receiveEnergy((long) (produced / 20.0), false); // Вт → Дж/тик
        status = produced > 1_000 ? STATUS_WORKING : STATUS_IDLE;
        updateBlockState(produced > 1_000);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.electric_generator");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
