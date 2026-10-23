package com.terrafabrica.content.era1;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
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
 * Ветряк: до 20 кВт, КПД 50%. Мощность зависит от погоды (ясно > дождь >
 * гроза) и высоты установки. Производит вращение.
 */
public class WindmillBlockEntity extends AbstractMachineBlockEntity {

    public WindmillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                3_000, 400,
                120, 400,
                4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        double weather = level.isThundering() ? 0.3 : level.isRaining() ? 0.5 : 1.0;
        double height = Units.clamp(0.5 + worldPosition.getY() / 128.0, 0.5, 1.5);
        double power = TFServerConfig.WINDMILL_MAX_POWER.get() * weather * height
                * TFServerConfig.WINDMILL_EFFICIENCY.get();
        double rpm = 40 + 80 * weather;
        rotation.setPower(power, rpm);
        updateBlockState(power > 100);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.windmill");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
