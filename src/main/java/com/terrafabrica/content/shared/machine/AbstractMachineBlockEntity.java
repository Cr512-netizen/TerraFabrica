package com.terrafabrica.content.shared.machine;

import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.content.shared.recipe.MachineRecipeFinder;
import com.terrafabrica.content.shared.recipe.MachineRecipeInput;
import com.terrafabrica.core.api.energy.EnergyStorage;
import com.terrafabrica.core.api.energy.IEnergyHandler;
import com.terrafabrica.core.api.heat.HeatStorage;
import com.terrafabrica.core.api.heat.IHeatHandler;
import com.terrafabrica.core.api.rotation.IRotationHandler;
import com.terrafabrica.core.api.rotation.RotationStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Базовый block entity всех машин Terra Fabrica.
 *
 * <p>Объединяет все «физические» capability мода в одном месте:
 * <ul>
 *   <li>энергия — {@link EnergyStorage}, джоули, приём/отдача с лимитами (Дж/тик);</li>
 *   <li>тепло — {@link HeatStorage}, K, пассивное остывание к 293.15 K;</li>
 *   <li>вращение — {@link RotationStorage}, RPM/Н·м, мощность P = (2π·RPM/60)·T;</li>
 *   <li>флюиды — два {@link FluidTank} (вход/выход), NeoForge {@link IFluidHandler};</li>
 *   <li>предметы — {@link ItemStackHandler} (входные слоты, затем выходные).</li>
 * </ul>
 *
 * <p>Рабочий цикл: машина ищет {@link MachineRecipe} своего {@link RecipeType},
 * фильтруя рецепты по исследованию игрока, открывшего GUI (см. {@link #opener}).
 * Энергия потребляется равномерно в течение цикла; если энергии/вращения не
 * хватает — цикл стоит. Температура/давление процесса проверяются по рецепту.
 *
 * <p>Все величины в СИ; синхронизация в GUI — через {@link ContainerData}.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity
        implements IEnergyHandler, IHeatHandler, IRotationHandler, IFluidHandler, MenuProvider {

    /** Индексы {@link ContainerData} (общий протокол GUI машин). */
    public static final int DATA_ENERGY_LO = 0;
    public static final int DATA_ENERGY_HI = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_TEMPERATURE_MK = 4;
    public static final int DATA_RPM = 5;
    public static final int DATA_TORQUE_CNM = 6; // сН·м (0.01 Н·м)
    public static final int DATA_POWER_W = 7;
    public static final int DATA_FLUID_IN = 8;
    public static final int DATA_FLUID_OUT = 9;
    public static final int DATA_STATUS = 10;
    public static final int DATA_SIZE = 11;

    /** Статус машины (в DATA_STATUS). */
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_NO_POWER = 2;
    public static final int STATUS_NO_RESEARCH = 3;
    public static final int STATUS_OUTPUT_FULL = 4;
    public static final int STATUS_COLD = 5;      // не хватает температуры
    public static final int STATUS_NO_FLUID = 6;
    public static final int STATUS_LOW_PRESSURE = 7;
    public static final int STATUS_NO_STRUCTURE = 8; // мультиблок не собран

    protected final EnergyStorage energy;
    protected final HeatStorage heat;
    protected final RotationStorage rotation;
    protected final FluidTank inputTank;
    protected final FluidTank outputTank;
    protected final ItemStackHandler inventory;
    /** Число входных предметных слотов (слоты 0..n-1). */
    private final int inputSlotCount;
    /** Число выходных предметных слотов (слоты n..n+m-1). */
    private final int outputSlotCount;

    /** Прогресс текущего цикла, тик. */
    protected int progress;
    /** Длительность текущего цикла, тик. */
    protected int maxProgress = 1;
    /** Активный рецепт (найденный в текущем цикле). */
    @Nullable
    protected MachineRecipe currentRecipe;
    /** Игрок, открывший GUI последним (для проверки исследований). */
    @Nullable
    protected UUID opener;
    /** Статус для GUI. */
    protected int status = STATUS_IDLE;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LO -> (int) (energy.getStoredEnergy() & 0xFFFFFFFFL);
                case DATA_ENERGY_HI -> (int) (energy.getStoredEnergy() >>> 32);
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_TEMPERATURE_MK -> (int) Math.round(heat.getTemperature() * 1000.0);
                case DATA_RPM -> (int) Math.round(rotation.getRPM());
                case DATA_TORQUE_CNM -> (int) Math.round(rotation.getTorque() * 100.0);
                case DATA_POWER_W -> (int) Math.round(rotation.getPower());
                case DATA_FLUID_IN -> inputTank.getFluidAmount();
                case DATA_FLUID_OUT -> outputTank.getFluidAmount();
                case DATA_STATUS -> status;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Только для чтения: сервер — источник истины.
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };

    /**
     * @param type           тип BE
     * @param pos            позиция
     * @param state          состояние блока
     * @param capacityJ      ёмкость энергии, Дж
     * @param maxInputJ      приём, Дж/тик
     * @param maxOutputJ     отдача, Дж/тик
     * @param heatCapacity   теплоёмкость, Дж/К
     * @param maxTempK       максимальная температура, K
     * @param maxRpm         предел оборотов привода
     * @param maxTorque      предел момента привода, Н·м
     * @param inputSlots     число входных слотов
     * @param outputSlots    число выходных слотов
     * @param inputTankCap   ёмкость входного бака, мБ
     * @param outputTankCap  ёмкость выходного бака, мБ
     */
    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         long capacityJ, long maxInputJ, long maxOutputJ,
                                         double heatCapacity, double maxTempK,
                                         double maxRpm, double maxTorque,
                                         int inputSlots, int outputSlots,
                                         int inputTankCap, int outputTankCap) {
        super(type, pos, state);
        this.energy = new EnergyStorage(capacityJ, maxInputJ, maxOutputJ);
        this.heat = new HeatStorage(heatCapacity, maxTempK, 0.5);
        this.rotation = new RotationStorage(maxRpm, maxTorque);
        this.inventory = new ItemStackHandler(inputSlots + outputSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.inputSlotCount = Math.max(0, inputSlots);
        this.outputSlotCount = Math.max(0, outputSlots);
        this.inputTank = new FluidTank(inputTankCap);
        this.outputTank = new FluidTank(outputTankCap) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    // ------------------------------------------------------------------
    // Абстрактные параметры машины
    // ------------------------------------------------------------------

    /** Тип рецептов машины. */
    public abstract RecipeType<MachineRecipe> recipeType();

    /** Число входных предметных слотов (задано в конструкторе). */
    public int inputSlots() {
        return inputSlotCount;
    }

    /** Число выходных предметных слотов (задано в конструкторе). */
    public int outputSlots() {
        return outputSlotCount;
    }

    /** Название машины (для GUI). */
    @Override
    public abstract Component getDisplayName();

    /** Создаёт меню GUI. */
    @Override
    @Nullable
    public abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player);

    // ------------------------------------------------------------------
    // Тик
    // ------------------------------------------------------------------

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof AbstractMachineBlockEntity machine && !level.isClientSide) {
            machine.tickServer();
        }
    }

    protected void tickServer() {
        heat.coolToAmbient(false);
        // Привод подключается каждый тик: тяга от соседнего вала/источника.
        pullRotation();
        double availablePower = rotation.getPower();
        double availableRpm = rotation.getRPM();
        rotation.reset();
        boolean active = tryWork(availablePower, availableRpm);
        updateBlockState(active);
    }

    /** Подхват вращения от соседнего блока (вал, колесо, двигатель). */
    protected void pullRotation() {
        for (Direction side : Direction.values()) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(worldPosition.relative(side));
            if (be instanceof IRotationHandler handler && handler != this && handler.getPower() > 0) {
                rotation.receiveRotation(handler.getRPM(), handler.getTorque());
                return;
            }
        }
    }

    /**
     * Главный рабочий цикл. Возвращает {@code true}, если машина активна
     * (для анимаций/звука). {@code availablePower} — механическая мощность
     * привода, Вт (от сети валов); {@code availableRpm} — его обороты.
     */
    protected boolean tryWork(double availablePower, double availableRpm) {
        // Ингредиенты изъяли — сбрасываем цикл.
        if (currentRecipe != null && !currentRecipe.matches(recipeInput(), level)) {
            currentRecipe = null;
            progress = 0;
        }
        if (currentRecipe == null) {
            Optional<RecipeHolder<MachineRecipe>> found = findRecipe();
            if (found.isEmpty()) {
                status = MachineRecipeFinder.hasLockedCandidate(level, recipeType(), recipeInput(), openerPlayer())
                        ? STATUS_NO_RESEARCH : STATUS_IDLE;
                progress = 0;
                return false;
            }
            currentRecipe = found.get().value();
            maxProgress = Math.max(1, currentRecipe.ticks());
            progress = 0;
        }

        // Проверка условий рецепта.
        if (!conditionsMet(currentRecipe)) {
            return false;
        }

        // Потребление энергии (Дж/тик в течение цикла).
        long energyPerTick = Math.max(1, currentRecipe.energy() / Math.max(1, currentRecipe.ticks()));
        if (energyPerTick > energy.getStoredEnergy() && availablePower < requiredPowerWatts()) {
            status = STATUS_NO_POWER;
            return false;
        }
        energy.extractEnergy(energyPerTick, false);

        // Потребление входного флюида (равномерно по циклу).
        if (currentRecipe.inputFluid() != null && !currentRecipe.inputFluid().isEmpty()) {
            int perTick = Math.max(1, currentRecipe.inputFluid().getAmount() / Math.max(1, currentRecipe.ticks()));
            if (inputTank.getFluidAmount() < perTick || !inputTank.getFluid().is(currentRecipe.inputFluid().getFluid())) {
                status = STATUS_NO_FLUID;
                return false;
            }
            inputTank.drain(perTick, FluidAction.EXECUTE);
        }

        status = STATUS_WORKING;
        progress++;
        if (progress >= maxProgress) {
            completeCycle();
        }
        return true;
    }

    /** Проверка температуры/давления и места в выходе. */
    protected boolean conditionsMet(MachineRecipe recipe) {
        if (recipe.minTemperature() > 0 && heat.getTemperature() < recipe.minTemperature()) {
            status = STATUS_COLD;
            return false;
        }
        if (recipe.minPressure() > 0 && pressurePa() < recipe.minPressure()) {
            status = STATUS_LOW_PRESSURE;
            return false;
        }
        if (!canFitOutputs(recipe)) {
            status = STATUS_OUTPUT_FULL;
            return false;
        }
        return true;
    }

    /** Давление процесса, Па (переопределяют котлы/реакторы). */
    public double pressurePa() {
        return 101_325.0; // атмосферное
    }

    /** Требуемая механическая мощность привода, Вт (0 — не требуется). */
    protected double requiredPowerWatts() {
        return 0;
    }

    /** Можно ли разместить все результаты рецепта в выходных слотах. */
    protected boolean canFitOutputs(MachineRecipe recipe) {
        for (int i = 0; i < recipe.results().size(); i++) {
            ItemStack result = recipe.getResultItem(i);
            int targetSlot = inputSlots() + i;
            ItemStack existing = inventory.getStackInSlot(targetSlot);
            if (!existing.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(existing, result)
                    || existing.getCount() + result.getCount() > existing.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    /** Завершение цикла: изъятие входов, выдача результатов, сброс прогресса. */
    protected void completeCycle() {
        MachineRecipe recipe = currentRecipe;
        if (recipe == null) {
            progress = 0;
            return;
        }
        if (!canFitOutputs(recipe)) {
            status = STATUS_OUTPUT_FULL;
            return; // прогресс застывает на максимуме
        }
        // Изъятие входов (точное количество по рецепту — весовые дозаторы).
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            inventory.getStackInSlot(i).shrink(recipe.count(i));
        }
        // Выдача результатов.
        for (int i = 0; i < recipe.results().size(); i++) {
            ItemStack result = recipe.getResultItem(i);
            int targetSlot = inputSlots() + i;
            ItemStack existing = inventory.getStackInSlot(targetSlot);
            if (existing.isEmpty()) {
                inventory.setStackInSlot(targetSlot, result);
            } else {
                existing.grow(result.getCount());
            }
        }
        // Выдача выходного флюида.
        if (recipe.outputFluid() != null && !recipe.outputFluid().isEmpty()) {
            outputTank.fill(recipe.outputFluid().copy(), FluidAction.EXECUTE);
        }
        progress = 0;
        currentRecipe = null;
        setChanged();
    }

    // ------------------------------------------------------------------
    // Поиск рецепта
    // ------------------------------------------------------------------

    protected MachineRecipeInput recipeInput() {
        ItemStack[] items = new ItemStack[inputSlots()];
        for (int i = 0; i < inputSlots(); i++) {
            items[i] = inventory.getStackInSlot(i);
        }
        return new MachineRecipeInput(items);
    }

    protected Optional<RecipeHolder<MachineRecipe>> findRecipe() {
        return MachineRecipeFinder.find(level, recipeType(), recipeInput(), openerPlayer());
    }

    /** Игрок, открывший GUI (для гейтинга исследований). */
    @Nullable
    public Player openerPlayer() {
        if (opener == null || level == null || level.isClientSide) return null;
        return level.getServer() == null ? null : level.getServer().getPlayerList().getPlayer(opener);
    }

    public void setOpener(@Nullable UUID opener) {
        this.opener = opener;
    }

    // ------------------------------------------------------------------
    // IEnergyHandler (делегирование)
    // ------------------------------------------------------------------

    @Override
    public long getStoredEnergy() {
        return energy.getStoredEnergy();
    }

    @Override
    public long getCapacity() {
        return energy.getCapacity();
    }

    @Override
    public long receiveEnergy(long amount, boolean simulate) {
        return energy.receiveEnergy(amount, simulate);
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        return energy.extractEnergy(amount, simulate);
    }

    @Override
    public long getMaxInput() {
        return energy.getMaxInput();
    }

    @Override
    public long getMaxOutput() {
        return energy.getMaxOutput();
    }

    /** Может ли машина обмениваться энергией через эту сторону (переопределяемо). */
    public boolean canConnectEnergy(@Nullable Direction side) {
        return true;
    }

    // ------------------------------------------------------------------
    // IHeatHandler (делегирование)
    // ------------------------------------------------------------------

    @Override
    public double getTemperature() {
        return heat.getTemperature();
    }

    @Override
    public double getHeatCapacity() {
        return heat.getHeatCapacity();
    }

    @Override
    public double getHeatEnergy() {
        return heat.getHeatEnergy();
    }

    @Override
    public double addHeat(double amount, boolean simulate) {
        return heat.addHeat(amount, simulate);
    }

    @Override
    public double removeHeat(double amount, boolean simulate) {
        return heat.removeHeat(amount, simulate);
    }

    @Override
    public double getMaxTemperature() {
        return heat.getMaxTemperature();
    }

    @Override
    public double getHeatTransferCoefficient() {
        return heat.getHeatTransferCoefficient();
    }

    // ------------------------------------------------------------------
    // IRotationHandler (делегирование)
    // ------------------------------------------------------------------

    @Override
    public double getRPM() {
        return rotation.getRPM();
    }

    @Override
    public double getTorque() {
        return rotation.getTorque();
    }

    @Override
    public boolean canConnect(Direction side) {
        return true;
    }

    @Override
    public boolean receiveRotation(double rpm, double torque) {
        return rotation.receiveRotation(rpm, torque);
    }

    // ------------------------------------------------------------------
    // IFluidHandler (вход/выход)
    // ------------------------------------------------------------------

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? inputTank.getFluid() : outputTank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? inputTank.getCapacity() : outputTank.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return inputTank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return outputTank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return outputTank.drain(maxDrain, action);
    }

    // ------------------------------------------------------------------
    // Состояние блока (LIT)
    // ------------------------------------------------------------------

    protected void updateBlockState(boolean active) {
        BlockState state = getBlockState();
        if (state.hasProperty(MachineBlock.LIT)) {
            boolean lit = state.getValue(MachineBlock.LIT);
            if (lit != active) {
                level.setBlock(worldPosition, state.setValue(MachineBlock.LIT, active), 3);
            }
        }
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putLong("energy", energy.getStoredEnergy());
        tag.putDouble("temperature", heat.getTemperature());
        CompoundTag fluidTag = new CompoundTag();
        fluidTag.put("input", inputTank.writeToNBT(registries, new CompoundTag()));
        fluidTag.put("output", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("fluids", fluidTag);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        if (opener != null) {
            tag.putUUID("opener", opener);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energy.setEnergy(tag.getLong("energy"));
        heat.setTemperature(tag.getDouble("temperature"));
        CompoundTag fluidTag = tag.getCompound("fluids");
        inputTank.readFromNBT(registries, fluidTag.getCompound("input"));
        outputTank.readFromNBT(registries, fluidTag.getCompound("output"));
        progress = tag.getInt("progress");
        maxProgress = Math.max(1, tag.getInt("maxProgress"));
        if (tag.hasUUID("opener")) {
            opener = tag.getUUID("opener");
        }
    }

    // ------------------------------------------------------------------
    // GUI-доступ
    // ------------------------------------------------------------------

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public EnergyStorage energyStorage() {
        return energy;
    }

    public HeatStorage heatStorage() {
        return heat;
    }

    public RotationStorage rotationStorage() {
        return rotation;
    }

    public FluidTank inputTank() {
        return inputTank;
    }

    public FluidTank outputTank() {
        return outputTank;
    }

    public int progress() {
        return progress;
    }

    public int maxProgress() {
        return maxProgress;
    }

    /** Открывает GUI игроку (серверная часть). */
    public void openGui(ServerPlayer player) {
        setOpener(player.getUUID());
        player.openMenu(this, worldPosition);
    }

    /** Сбрасывает содержимое при разрушении. */
    public void dropContents(Level level, BlockPos pos) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < inventory.getSlots(); i++) {
            items.add(inventory.getStackInSlot(i));
        }
        net.minecraft.world.Containers.dropContents(level, pos, items);
    }
}
