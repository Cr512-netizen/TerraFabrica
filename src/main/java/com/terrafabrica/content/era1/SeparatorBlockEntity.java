package com.terrafabrica.content.era1;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.TFRecipeTypes;
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

import java.util.Random;

/**
 * Сепаратор: 2 порошка → 1 концентрат (базовый выход 50% массы).
 *
 * <p>Дополнительные правила (см. TFServerConfig):
 * <ul>
 *   <li>богатая порода: +25% шанс получить дополнительный концентрат;</li>
 *   <li>бедная порода: 25% шанс потерять концентрат.</li>
 * </ul>
 * 50 кДж/цикл, 5 с; привод — вращение или электричество.
 *
 * <p>Поскольку порошки одного минерала от разных пород неразличимы, шансы
 * богатой/бедной породы моделируются статистически: каждая партия порошков
 * получает «происхождение» по распределению 30/60/10 жилы — это и даёт
 * средний выход, согласованный с балансом (см. docs/BALANCE.md).
 */
public class SeparatorBlockEntity extends AbstractMachineBlockEntity {

    private final Random random = new Random();

    public SeparatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                400_000, 200_000, 0,
                2_000, 500,
                1_200, 1_500,
                4, 4,
                0, 0);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.SEPARATOR.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.separator");
    }

    @Override
    protected double requiredPowerWatts() {
        return (double) TFServerConfig.SEPARATOR_ENERGY_PER_CYCLE.get()
                / TFServerConfig.SEPARATOR_CYCLE_TICKS.get() * 20.0;
    }

    @Override
    protected void completeCycle() {
        MachineRecipe recipe = currentRecipe;
        if (recipe == null) {
            progress = 0;
            return;
        }
        if (!canFitOutputs(recipe)) {
            status = STATUS_OUTPUT_FULL;
            return;
        }
        // Изъятие входов: ровно 2 порошка (базовый выход концентрата — 50% массы).
        inventory.getStackInSlot(0).shrink(2);
        // Выход с учётом происхождения порошка (30/60/10 → бедная/обычная/богатая).
        double roll = random.nextDouble();
        boolean rich = roll >= 0.9;
        boolean poor = roll < 0.3;

        for (int i = 0; i < recipe.results().size(); i++) {
            net.minecraft.world.item.ItemStack result = recipe.getResultItem(i);
            int targetSlot = 4 + i; // выходные слоты 4..7
            net.minecraft.world.item.ItemStack existing = inventory.getStackInSlot(targetSlot);
            if (poor && random.nextDouble() < TFServerConfig.CONCENTRATE_POOR_LOSS.get()) {
                continue; // бедная порода: концентрат потерян
            }
            int count = result.getCount();
            if (rich && random.nextDouble() < TFServerConfig.CONCENTRATE_RICH_BONUS.get()) {
                count += 1; // богатая: дополнительный концентрат
            }
            if (existing.isEmpty()) {
                inventory.setStackInSlot(targetSlot, result.copyWithCount(count));
            } else {
                existing.grow(count);
            }
        }
        if (recipe.outputFluid() != null && !recipe.outputFluid().isEmpty()) {
            outputTank.fill(recipe.outputFluid().copy(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        }
        progress = 0;
        currentRecipe = null;
        setChanged();
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
