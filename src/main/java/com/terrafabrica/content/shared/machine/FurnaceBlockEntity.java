package com.terrafabrica.content.shared.machine;

import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * База топливных печей: сжигает топливо, поднимая температуру, и выполняет
 * рецепты с требованием {@code min_temperature}.
 *
 * <p>Раскладка слотов (общая сетка 4 входа + 4 выхода):
 * <ul>
 *   <li>0–2 — входные предметы рецепта;</li>
 *   <li>3 — топливо;</li>
 *   <li>4–7 — выходы.</li>
 * </ul>
 * Печь не остывает мгновенно; топливо не сжигается при достижении максимума
 * температуры (автоматическая экономия).
 */
public abstract class FurnaceBlockEntity extends AbstractMachineBlockEntity {

    /** Слот топлива. */
    public static final int FUEL_SLOT = 3;

    /** Логика топки. */
    protected final BurnerLogic burner;

    protected FurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                 double maxTempK, double heatCapacity, double burnPower) {
        super(type, pos, state,
                0, 0, 0,               // печи не работают с электричеством (кроме EAF — отдельно)
                heatCapacity, maxTempK,
                0, 0,                  // печи не требуют вращения
                4, 4,
                8_000, 8_000);
        this.burner = new BurnerLogic(heat, burnPower);
    }

    @Override
    protected void tickServer() {
        heat.coolToAmbient(false);
        burner.tick(inventory, FUEL_SLOT);
        boolean active = tryWork(0, 0);
        updateBlockState(active);
    }

    /** Активно ли пламя (для анимаций). */
    public boolean isBurning() {
        return burner.isBurning();
    }

    /** Доступен ли процесс по температуре (рецепт уже проверяет min_temperature). */
    @Override
    protected boolean conditionsMet(MachineRecipe recipe) {
        return super.conditionsMet(recipe);
    }

    @Override
    protected double requiredPowerWatts() {
        return 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + blockId());
    }

    /** id блока (для названия GUI). */
    protected abstract String blockId();

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MachineMenu(containerId, playerInventory, this, dataAccess);
    }

    /** Стандартный тикер для всех печей. */
    public static <T extends BlockEntityType<?>> void tick(Level level, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity be) {
        if (be instanceof FurnaceBlockEntity furnace && !level.isClientSide) {
            furnace.tickServer();
        }
    }

    /** Доступ к баку входного флюида с проверкой (удобство для подклассов). */
    protected boolean hasFluid(FluidStack stack) {
        return inputTank.getFluidAmount() >= stack.getAmount() && inputTank.getFluid().is(stack.getFluid());
    }
}
