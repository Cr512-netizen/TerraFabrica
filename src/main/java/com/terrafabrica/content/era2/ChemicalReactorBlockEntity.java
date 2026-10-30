package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.util.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Химический реактор (один класс на все химические машины Эпохи II;
 * режим определяется по блоку через {@link #MODE_BY_BLOCK}).
 *
 * <p>Режимы:
 * <ul>
 *   <li>{@link Mode#GENERIC} — обычные машинные рецепты с флюидами
 *       (свинцовая камера, абсорбер, Сольве, удобрения);</li>
 *   <li>{@link Mode#ELECTROLYSIS} — электролиз воды: H₂O → 2H₂ + O₂;</li>
 *   <li>{@link Mode#CHLORALKALI} — электролиз рассола: NaCl + H₂O → NaOH + Cl₂ + H₂;</li>
 *   <li>{@link Mode#ASU} — воздухоразделение: воздух → N₂ + O₂ (криогенное);</li>
 *   <li>{@link Mode#HABER} — синтез аммиака 400–500 °C, 20–30 МПа, железный
 *       катализатор (отравляется серой);</li>
 *   <li>{@link Mode#OSTWALD} — азотная кислота на платино-родиевых сетках
 *       (800–900 °C, сетки теряют активность);</li>
 *   <li>{@link Mode#CONTACT} — контактная серная кислота (450 °C, ванадиевый
 *       катализатор, до 98%);</li>
 *   <li>{@link Mode#ALUMINUM} — электролиз алюминия (960 °C, криолит,
 *       улавливание фтора);</li>
 *   <li>{@link Mode#DISTILLATION} — перегонка нефти на фракции.</li>
 * </ul>
 */
public class ChemicalReactorBlockEntity extends MultiblockMachineBlockEntity {

    public enum Mode {
        GENERIC, ELECTROLYSIS, CHLORALKALI, ASU, HABER, OSTWALD, CONTACT, ALUMINUM, DISTILLATION
    }

    /** Слот катализатора. */
    public static final int CATALYST_SLOT = 3;

    /** Режим по блоку. */
    public static final Map<Block, Mode> MODE_BY_BLOCK = Map.ofEntries(
            Map.entry(TFBlocks.LEAD_CHAMBER.get(), Mode.GENERIC),
            Map.entry(TFBlocks.ABSORBER.get(), Mode.GENERIC),
            Map.entry(TFBlocks.SOLVAY_REACTOR.get(), Mode.GENERIC),
            Map.entry(TFBlocks.FERTILIZER_MIXER.get(), Mode.GENERIC),
            Map.entry(TFBlocks.WATER_ELECTROLYZER.get(), Mode.ELECTROLYSIS),
            Map.entry(TFBlocks.CHLORALKALI_CELL.get(), Mode.CHLORALKALI),
            Map.entry(TFBlocks.AIR_SEPARATION_UNIT.get(), Mode.ASU),
            Map.entry(TFBlocks.AMMONIA_SYNTHESIZER.get(), Mode.HABER),
            Map.entry(TFBlocks.OSTWALD_REACTOR.get(), Mode.OSTWALD),
            Map.entry(TFBlocks.CONTACT_ACID_REACTOR.get(), Mode.CONTACT),
            Map.entry(TFBlocks.ALUMINUM_ELECTROLYZER.get(), Mode.ALUMINUM),
            Map.entry(TFBlocks.OIL_DISTILLATION_COLUMN.get(), Mode.DISTILLATION));

    /** Дополнительные баки (для машин с несколькими выходами). */
    private final FluidTank[] extraTanks = new FluidTank[]{
            new FluidTank(16_000), new FluidTank(16_000)
    };

    private int cycleTimer;
    private boolean hasSulfur;

    public ChemicalReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                10_000_000, 300_000, 0,
                100_000, 1_200.15,
                0, 0,
                16_000, 16_000,
                structureFor(type, pos, state));
    }

    /** Структура по блоку (для мультиблочных реакторов). */
    private static List<MultiblockHelper.Part> structureFor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        // Определяется при первом тике — здесь возвращаем пустую раскладку;
        // фактическая раскладка подставляется в initStructure().
        return List.of();
    }

    private Mode mode() {
        Mode mode = MODE_BY_BLOCK.get(getBlockState().getBlock());
        return mode != null ? mode : Mode.GENERIC;
    }

    private void initStructure() {
        Block block = getBlockState().getBlock();
        if (block == TFBlocks.LEAD_CHAMBER.get()) {
            structureSet(MultiblockHelper.solidBox(5, 5, 5, TFBlocks.LEAD_CASING.get()));
        } else if (block == TFBlocks.ABSORBER.get()) {
            structureSet(MultiblockHelper.solidBox(2, 3, 2, TFBlocks.LEAD_CASING.get()));
        } else if (block == TFBlocks.SOLVAY_REACTOR.get()) {
            structureSet(MultiblockHelper.solidBox(3, 4, 3, TFBlocks.REACTOR_CASING.get()));
        } else if (block == TFBlocks.AMMONIA_SYNTHESIZER.get()) {
            structureSet(MultiblockHelper.solidBox(4, 6, 4, TFBlocks.STEAM_CASING.get()));
        } else if (block == TFBlocks.OSTWALD_REACTOR.get()) {
            structureSet(MultiblockHelper.solidBox(4, 5, 4, TFBlocks.REACTOR_CASING.get()));
        } else if (block == TFBlocks.CONTACT_ACID_REACTOR.get()) {
            structureSet(MultiblockHelper.solidBox(4, 5, 4, TFBlocks.REACTOR_CASING.get()));
        } else if (block == TFBlocks.CHLORALKALI_CELL.get()) {
            structureSet(MultiblockHelper.solidBox(3, 4, 3, TFBlocks.REACTOR_CASING.get()));
        } else if (block == TFBlocks.ALUMINUM_ELECTROLYZER.get()) {
            structureSet(MultiblockHelper.solidBox(4, 5, 4, TFBlocks.REFRACTORY_BRICK.get()));
        } else if (block == TFBlocks.AIR_SEPARATION_UNIT.get()) {
            structureSet(MultiblockHelper.solidBox(4, 6, 4, TFBlocks.IRON_CASING.get()));
        } else if (block == TFBlocks.OIL_DISTILLATION_COLUMN.get()) {
            structureSet(MultiblockHelper.solidBox(2, 8, 2, TFBlocks.STEAM_CASING.get()));
        } else if (block == TFBlocks.WATER_ELECTROLYZER.get() || block == TFBlocks.FERTILIZER_MIXER.get()) {
            structureSet(List.of()); // одноблочные
        } else {
            structureSet(List.of());
        }
    }

    /** Замена раскладки структуры (поле родителя final-список — копируем). */
    private void structureSet(List<MultiblockHelper.Part> parts) {
        structure.clear();
        structure.addAll(parts);
    }

    @Override
    protected void tickServer() {
        if (structure.isEmpty() && !structureInitialized) {
            initStructure();
            structureInitialized = true;
        }
        if (--checkCooldown <= 0) {
            checkCooldown = STRUCTURE_CHECK_INTERVAL;
            formed = structure.isEmpty() || checkStructure();
        }
        if (!formed) {
            status = STATUS_NO_STRUCTURE;
            updateBlockState(false);
            return;
        }
        heat.coolToAmbient(false);
        // Электрический подогрев для высокотемпературных режимов (Габер, Оствальд,
        // контактный, электролиз алюминия, перегонка): энергия → тепло, КПД 90%.
        Mode mode = mode();
        if (needsHeating(mode)) {
            long draw = energy.extractEnergy(2_500, false); // 50 кВт
            if (draw > 0) {
                heat.addHeat(draw * 0.9, false);
            }
        }
        boolean active;
        switch (mode) {
            case ELECTROLYSIS -> active = tickElectrolysis(false);
            case CHLORALKALI -> active = tickElectrolysis(true);
            case ASU -> active = tickAsu();
            case HABER -> active = tickHaber();
            case OSTWALD -> active = tickOstwald();
            case CONTACT -> active = tickContact();
            case ALUMINUM -> active = tickAluminum();
            case DISTILLATION -> active = tickDistillation();
            default -> active = tryWork(0, 0);
        }
        updateBlockState(active);
    }

    private boolean structureInitialized;

    /** Режимы, требующие электрического подогрева. */
    private static boolean needsHeating(Mode mode) {
        return switch (mode) {
            case HABER, OSTWALD, CONTACT, ALUMINUM, DISTILLATION -> true;
            default -> false;
        };
    }

    // ------------------------------------------------------------------
    // Режимы
    // ------------------------------------------------------------------

    /** Электролиз воды / рассола: энергия + вода (+соль) → газы (+щёлочь). */
    private boolean tickElectrolysis(boolean chloralkali) {
        // Энергия: 20 кВт (1 кДж/тик) на электролизёр.
        long perTick = chloralkali ? 2_000 : 1_000;
        if (energy.extractEnergy(perTick, false) < perTick || inputTank.getFluidAmount() < 1) {
            status = energy.getStoredEnergy() < perTick ? STATUS_NO_POWER : STATUS_NO_FLUID;
            return false;
        }
        inputTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
        cycleTimer++;
        status = STATUS_WORKING;
        if (cycleTimer >= 100) {
            cycleTimer = 0;
            // Исправленная стехиометрия: 2 H₂O → 2 H₂ + O₂
            // 100 мБ воды → 2 мБ H₂ + 1 мБ O₂ (соотношение 2:1)
            outputTank.fill(new FluidStack(TFFluidsSource.HYDROGEN, 2), IFluidHandler.FluidAction.EXECUTE);
            outputTank.fill(new FluidStack(TFFluidsSource.OXYGEN, 1), IFluidHandler.FluidAction.EXECUTE);
            if (chloralkali) {
                // Хлорщелочная ячейка: 2 NaCl + 2 H₂O → Cl₂ + H₂ + 2 NaOH
                // Расходуется рассол, а не вода
                extraTanks[0].fill(new FluidStack(TFFluidsSource.CHLORINE, 1), IFluidHandler.FluidAction.EXECUTE);
                // Каустическая сода — предмет в выходном слоте 4.
                net.minecraft.world.item.ItemStack naoh =
                        new net.minecraft.world.item.ItemStack(com.terrafabrica.content.TFItems.CAUSTIC_SODA.get());
                var existing = inventory.getStackInSlot(4);
                if (existing.isEmpty()) {
                    inventory.setStackInSlot(4, naoh);
                } else if (existing.is(naoh.getItem())) {
                    existing.grow(1);
                } else {
                    status = STATUS_OUTPUT_FULL;
                    return false;
                }
            }
        }
        return true;
    }

    /** Воздухоразделительная установка: энергия → N₂ + O₂. */
    private boolean tickAsu() {
        long perTick = 5_000; // 100 кВт
        if (energy.extractEnergy(perTick, false) < perTick) {
            status = STATUS_NO_POWER;
            return false;
        }
        cycleTimer++;
        status = STATUS_WORKING;
        if (cycleTimer >= 200) {
            cycleTimer = 0;
            outputTank.fill(new FluidStack(TFFluidsSource.NITROGEN, 8), IFluidHandler.FluidAction.EXECUTE);
            outputTank.fill(new FluidStack(TFFluidsSource.OXYGEN, 2), IFluidHandler.FluidAction.EXECUTE);
        }
        return true;
    }

    /** Синтез аммиака: N₂ + 3H₂ → 2NH₃, 400–500 °C, 20–30 МПа, Fe-катализатор. */
    private boolean tickHaber() {
        double t = heat.getTemperature();
        double p = pressurePa();
        if (t < 673.15 || t > 773.15 || p < 20_000_000) {
            status = t < 673.15 ? STATUS_COLD : STATUS_LOW_PRESSURE;
            return false;
        }
        var catalyst = inventory.getStackInSlot(CATALYST_SLOT);
        if (catalyst.isEmpty()) {
            status = STATUS_NO_RESEARCH; // нет катализатора (переиспользуем статус «заблокировано»)
            return false;
        }
        // Сера отравляет катализатор: ускоренная деградация.
        int degrade = hasSulfur ? 10 : 1;
        if (inputTank.getFluidAmount() >= 4 && energy.extractEnergy(2_000, false) >= 2_000) {
            // Drain 4 mB: 1 N₂ + 3 H₂
            inputTank.drain(4, IFluidHandler.FluidAction.EXECUTE);
            cycleTimer += degrade;
            status = STATUS_WORKING;
            if (cycleTimer >= 600) {
                cycleTimer = 0;
                // 2 mB NH₃ на цикл при правильном соотношении
                outputTank.fill(new FluidStack(TFFluidsSource.AMMONIA, 2), IFluidHandler.FluidAction.EXECUTE);
                damageCatalyst(catalyst, 1);
            }
            return true;
        }
        status = STATUS_NO_FLUID;
        return false;
    }

    /** Оствальд: NH₃ + O₂ → HNO₃, 800–900 °C, Pt/Rh сетки. */
    private boolean tickOstwald() {
        double t = heat.getTemperature();
        if (t < 1_073.15 || t > 1_173.15) {
            status = STATUS_COLD;
            return false;
        }
        var catalyst = inventory.getStackInSlot(CATALYST_SLOT);
        if (catalyst.isEmpty() || inputTank.getFluidAmount() < 2) {
            status = catalyst.isEmpty() ? STATUS_NO_RESEARCH : STATUS_NO_FLUID;
            return false;
        }
        if (energy.extractEnergy(1_000, false) < 1_000) {
            status = STATUS_NO_POWER;
            return false;
        }
        inputTank.drain(2, IFluidHandler.FluidAction.EXECUTE);
        cycleTimer++;
        status = STATUS_WORKING;
        if (cycleTimer >= 400) {
            cycleTimer = 0;
            outputTank.fill(new FluidStack(TFFluidsSource.NITRIC_ACID, 1), IFluidHandler.FluidAction.EXECUTE);
            damageCatalyst(catalyst, 1);
        }
        return true;
    }

    /** Контактный способ: SO₂ + O₂ → H₂SO₄ (98%), 450 °C, V-катализатор. */
    private boolean tickContact() {
        double t = heat.getTemperature();
        if (t < 673.15 || t > 773.15) {
            status = STATUS_COLD;
            return false;
        }
        var catalyst = inventory.getStackInSlot(CATALYST_SLOT);
        if (catalyst.isEmpty() || inputTank.getFluidAmount() < 2) {
            status = catalyst.isEmpty() ? STATUS_NO_RESEARCH : STATUS_NO_FLUID;
            return false;
        }
        if (energy.extractEnergy(1_000, false) < 1_000) {
            status = STATUS_NO_POWER;
            return false;
        }
        inputTank.drain(2, IFluidHandler.FluidAction.EXECUTE);
        cycleTimer++;
        status = STATUS_WORKING;
        if (cycleTimer >= 400) {
            cycleTimer = 0;
            outputTank.fill(new FluidStack(TFFluidsSource.SULFURIC_ACID, 2), IFluidHandler.FluidAction.EXECUTE);
            damageCatalyst(catalyst, 1);
        }
        return true;
    }

    /** Электролиз алюминия: 960 °C, криолит, 13–15 МВт·ч/т, улавливание фтора. */
    private boolean tickAluminum() {
        double t = heat.getTemperature();
        if (t < 1_233.15) {
            status = STATUS_COLD;
            return false;
        }
        long perTick = 4_500; // 90 кВт → 15 МВт·ч/т за цикл 600 тик
        if (energy.extractEnergy(perTick, false) < perTick) {
            status = STATUS_NO_POWER;
            return false;
        }
        var alumina = inventory.getStackInSlot(0);
        if (alumina.isEmpty()) {
            status = STATUS_IDLE;
            return false;
        }
        cycleTimer++;
        status = STATUS_WORKING;
        if (cycleTimer >= 600) {
            cycleTimer = 0;
            alumina.shrink(1);
            var ingot = new net.minecraft.world.item.ItemStack(com.terrafabrica.content.TFItems.ALUMINUM_INGOT.get());
            var out = inventory.getStackInSlot(4);
            if (out.isEmpty()) {
                inventory.setStackInSlot(4, ingot);
            } else {
                out.grow(1);
            }
            // Фтор улавливается и возвращается в процесс (криолит).
            var fluoride = inventory.getStackInSlot(5);
            if (fluoride.isEmpty()) {
                inventory.setStackInSlot(5, new net.minecraft.world.item.ItemStack(
                        com.terrafabrica.content.TFItems.FLUORIDE_SALT.get()));
            } else {
                fluoride.grow(1);
            }
        }
        return true;
    }

    /** Перегонка нефти: нефть + тепло → бензин, керосин, дизель, мазут. */
    private boolean tickDistillation() {
        double t = heat.getTemperature();
        if (t < 573.15 || inputTank.getFluidAmount() < 10) {
            status = t < 573.15 ? STATUS_COLD : STATUS_NO_FLUID;
            return false;
        }
        inputTank.drain(10, IFluidHandler.FluidAction.EXECUTE);
        outputTank.fill(new FluidStack(TFFluidsSource.GASOLINE, 4), IFluidHandler.FluidAction.EXECUTE);
        outputTank.fill(new FluidStack(TFFluidsSource.KEROSENE, 2), IFluidHandler.FluidAction.EXECUTE);
        extraTanks[0].fill(new FluidStack(TFFluidsSource.DIESEL, 3), IFluidHandler.FluidAction.EXECUTE);
        extraTanks[1].fill(new FluidStack(TFFluidsSource.FUEL_OIL, 1), IFluidHandler.FluidAction.EXECUTE);
        status = STATUS_WORKING;
        return true;
    }

    /** Износ катализатора (слот 3; предмет с прочностью). */
    private void damageCatalyst(net.minecraft.world.item.ItemStack catalyst, int amount) {
        if (catalyst.isDamageableItem()) {
            catalyst.setDamageValue(catalyst.getDamageValue() + amount);
            if (catalyst.getDamageValue() >= catalyst.getMaxDamage()) {
                catalyst.shrink(1);
            }
        }
    }

    /** Отметка о сере во входном газе (вызывается при подаче SO₂). */
    public void markSulfur() {
        hasSulfur = true;
    }

    // ------------------------------------------------------------------
    // Флюиды: дополнительные баки
    // ------------------------------------------------------------------

    @Override
    public int getTanks() {
        return 4;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getFluid();
            case 1 -> outputTank.getFluid();
            default -> extraTanks[tank - 2].getFluid();
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getCapacity();
            case 1 -> outputTank.getCapacity();
            default -> extraTanks[tank - 2].getCapacity();
        };
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.getFluid().toString().contains("sulfur_dioxide")) {
            if (action.execute()) markSulfur();
            return resource.getAmount();
        }
        return inputTank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        FluidStack fromMain = outputTank.drain(resource, action);
        if (!fromMain.isEmpty()) return fromMain;
        for (FluidTank extra : extraTanks) {
            FluidStack drained = extra.drain(resource, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        FluidStack fromMain = outputTank.drain(maxDrain, action);
        if (!fromMain.isEmpty()) return fromMain;
        for (FluidTank extra : extraTanks) {
            FluidStack drained = extra.drain(maxDrain, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    // ------------------------------------------------------------------
    // Прочее
    // ------------------------------------------------------------------

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return switch (mode()) {
            case GENERIC -> {
                Block block = getBlockState().getBlock();
                if (block == TFBlocks.LEAD_CHAMBER.get()) yield TFRecipeTypes.LEAD_CHAMBER.get();
                if (block == TFBlocks.ABSORBER.get()) yield TFRecipeTypes.ABSORBER.get();
                if (block == TFBlocks.SOLVAY_REACTOR.get()) yield TFRecipeTypes.SOLVAY_REACTOR.get();
                yield TFRecipeTypes.FERTILIZER_MIXER.get();
            }
            case ELECTROLYSIS -> TFRecipeTypes.WATER_ELECTROLYZER.get();
            case CHLORALKALI -> TFRecipeTypes.CHLORALKALI_CELL.get();
            case ASU -> TFRecipeTypes.AIR_SEPARATION_UNIT.get();
            case HABER -> TFRecipeTypes.AMMONIA_SYNTHESIZER.get();
            case OSTWALD -> TFRecipeTypes.OSTWALD_REACTOR.get();
            case CONTACT -> TFRecipeTypes.CONTACT_ACID_REACTOR.get();
            case ALUMINUM -> TFRecipeTypes.ALUMINUM_ELECTROLYZER.get();
            case DISTILLATION -> TFRecipeTypes.OIL_DISTILLATION_COLUMN.get();
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + blockName());
    }

    private String blockName() {
        var block = getBlockState().getBlock();
        if (block == TFBlocks.LEAD_CHAMBER.get()) return "lead_chamber";
        if (block == TFBlocks.ABSORBER.get()) return "absorber";
        if (block == TFBlocks.SOLVAY_REACTOR.get()) return "solvay_reactor";
        if (block == TFBlocks.FERTILIZER_MIXER.get()) return "fertilizer_mixer";
        if (block == TFBlocks.WATER_ELECTROLYZER.get()) return "water_electrolyzer";
        if (block == TFBlocks.CHLORALKALI_CELL.get()) return "chloralkali_cell";
        if (block == TFBlocks.AIR_SEPARATION_UNIT.get()) return "air_separation_unit";
        if (block == TFBlocks.AMMONIA_SYNTHESIZER.get()) return "ammonia_synthesizer";
        if (block == TFBlocks.OSTWALD_REACTOR.get()) return "ostwald_reactor";
        if (block == TFBlocks.CONTACT_ACID_REACTOR.get()) return "contact_acid_reactor";
        if (block == TFBlocks.ALUMINUM_ELECTROLYZER.get()) return "aluminum_electrolyzer";
        return "oil_distillation_column";
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cycleTimer", cycleTimer);
        tag.putBoolean("hasSulfur", hasSulfur);
        tag.put("extra0", extraTanks[0].writeToNBT(registries, new net.minecraft.nbt.CompoundTag()));
        tag.put("extra1", extraTanks[1].writeToNBT(registries, new net.minecraft.nbt.CompoundTag()));
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        cycleTimer = tag.getInt("cycleTimer");
        hasSulfur = tag.getBoolean("hasSulfur");
        extraTanks[0].readFromNBT(registries, tag.getCompound("extra0"));
        extraTanks[1].readFromNBT(registries, tag.getCompound("extra1"));
    }

    /** Источник id флюидов (объявлены в TFFluids). */
    private static final class TFFluidsSource {
        static final net.minecraft.world.level.material.Fluid HYDROGEN =
                com.terrafabrica.content.TFFluids.HYDROGEN.get();
        static final net.minecraft.world.level.material.Fluid OXYGEN =
                com.terrafabrica.content.TFFluids.OXYGEN.get();
        static final net.minecraft.world.level.material.Fluid NITROGEN =
                com.terrafabrica.content.TFFluids.NITROGEN.get();
        static final net.minecraft.world.level.material.Fluid AMMONIA =
                com.terrafabrica.content.TFFluids.AMMONIA.get();
        static final net.minecraft.world.level.material.Fluid CHLORINE =
                com.terrafabrica.content.TFFluids.CHLORINE.get();
        static final net.minecraft.world.level.material.Fluid NITRIC_ACID =
                com.terrafabrica.content.TFFluids.NITRIC_ACID.get();
        static final net.minecraft.world.level.material.Fluid SULFURIC_ACID =
                com.terrafabrica.content.TFFluids.SULFURIC_ACID.get();
        static final net.minecraft.world.level.material.Fluid GASOLINE =
                com.terrafabrica.content.TFFluids.GASOLINE.get();
        static final net.minecraft.world.level.material.Fluid KEROSENE =
                com.terrafabrica.content.TFFluids.KEROSENE.get();
        static final net.minecraft.world.level.material.Fluid DIESEL =
                com.terrafabrica.content.TFFluids.DIESEL.get();
        static final net.minecraft.world.level.material.Fluid FUEL_OIL =
                com.terrafabrica.content.TFFluids.FUEL_OIL.get();
    }
}
