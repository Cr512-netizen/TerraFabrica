package com.terrafabrica.content.shared.machine;

import com.terrafabrica.core.api.heat.HeatStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Логика топки: сжигает топливо из слота, поднимая температуру {@link HeatStorage}.
 *
 * <p>Топливо не сжигается при приближении к предельной температуре (экономия),
 * и не разжигается, пока температура ниже 95% максимума.
 */
public final class BurnerLogic {

    private final HeatStorage heat;
    private final double burnPower; // Вт
    private int burnTicksRemaining;

    public BurnerLogic(HeatStorage heat, double burnPowerWatts) {
        this.heat = heat;
        this.burnPower = burnPowerWatts;
    }

    /** Тик топки. */
    public void tick(ItemStackHandler inventory, int fuelSlot) {
        if (burnTicksRemaining > 0) {
            burnTicksRemaining--;
            if (heat.getTemperature() < heat.getMaxTemperature() * 0.98) {
                heat.addHeat(burnPower / 20.0, false);
            }
        }
        if (burnTicksRemaining <= 0 && heat.getTemperature() < heat.getMaxTemperature() * 0.95) {
            ItemStack fuel = inventory.getStackInSlot(fuelSlot);
            int burnTime = fuel.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                fuel.shrink(1);
                burnTicksRemaining = burnTime;
            }
        }
    }

    public boolean isBurning() {
        return burnTicksRemaining > 0;
    }
}
