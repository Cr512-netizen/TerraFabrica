package com.terrafabrica.content.era2;

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

/**
 * Весовой дозатор: точное легирование «по весу, а не на глаз».
 *
 * <p>Рецепты задают точные количества каждого компонента (поле {@code counts},
 * пропорции = массовые доли сплава). Пример: нержавеющая сталь Fe-18Cr-8Ni —
 * 74 железа + 18 хрома + 8 никеля → 2 слитка. При нарушении состава рецепт
 * просто не выполняется (деталь не создаётся, ресурсы не тратятся).
 */
public class WeightDispenserBlockEntity extends AbstractMachineBlockEntity {

    public WeightDispenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                1_000, 400,
                0, 0,
                4, 4, 0, 0);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.WEIGHT_DISPENSER.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.weight_dispenser");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
