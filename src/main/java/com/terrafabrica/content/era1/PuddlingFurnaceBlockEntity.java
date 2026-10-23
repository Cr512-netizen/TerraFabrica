package com.terrafabrica.content.era1;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockFurnaceBlockEntity;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Пудлинговая печь (мультиблок 3×3×3): чугун → ковкое железо.
 * 1 чугун + 1 уголь + 30 с перемешивания → 0.8 железа (математическое ожидание:
 * 80% шанс получить слиток). Требует вращения для мешалки.
 */
public class PuddlingFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public static final double STIRRING_POWER_W = 300;
    private final Random random = new Random();

    public PuddlingFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1573.15, 60_000, 8_000,
                MultiblockHelper.solidBox(3, 3, 3, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "puddling_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.PUDDLING_FURNACE.get();
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
        double power = rotation.getPower();
        rotation.reset();
        heat.coolToAmbient(false);
        if (power < STIRRING_POWER_W) {
            // Без перемешивания печь не работает (но топливо не горит зря).
            status = STATUS_NO_POWER;
            progress = 0;
            currentRecipe = null;
            updateBlockState(false);
            return;
        }
        burner.tick(inventory, FUEL_SLOT);
        boolean active = tryWork(power, 0);
        updateBlockState(active);
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
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            inventory.getStackInSlot(i).shrink(1);
        }
        // Выход: 0.8 железа = 80% шанс слитка (математическое ожидание совпадает).
        if (random.nextDouble() < TFServerConfig.PUDDLING_IRON_YIELD.get()) {
            ItemStack iron = new ItemStack(Items.IRON_INGOT);
            ItemStack existing = inventory.getStackInSlot(4);
            if (existing.isEmpty()) {
                inventory.setStackInSlot(4, iron);
            } else {
                existing.grow(1);
            }
        }
        progress = 0;
        currentRecipe = null;
        setChanged();
    }
}
