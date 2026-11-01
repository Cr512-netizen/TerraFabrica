package com.terrafabrica.content.era3;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Машины Эпохи III: полупроводники, микросхемы, углеволокно, ядерная
 * энергетика, спутники, ЭЗС, телескоп. Одноблочные (SimpleMachineBlock),
 * логика — общая через AbstractMachineBlockEntity, тип рецепта выбирается по блоку.
 */
public class Era3MachineBlockEntity extends AbstractMachineBlockEntity {

    public enum Kind { SEMICONDUCTOR, TRANSISTOR, MICROCHIP, CARBON_FIBER, REACTOR_CORE,
                      ENRICHMENT, SATELLITE, FUSION, EV_CHARGER, TELESCOPE }

    public static final Map<Block, Kind> KIND_BY_BLOCK = Map.ofEntries(
            Map.entry(TFBlocks.SEMICONDUCTOR_FURNACE.get(), Kind.SEMICONDUCTOR),
            Map.entry(TFBlocks.TRANSISTOR_ASSEMBLER.get(), Kind.TRANSISTOR),
            Map.entry(TFBlocks.MICROCHIP_FAB.get(), Kind.MICROCHIP),
            Map.entry(TFBlocks.CARBON_FIBER_PLANT.get(), Kind.CARBON_FIBER),
            Map.entry(TFBlocks.REACTOR_CORE.get(), Kind.REACTOR_CORE),
            Map.entry(TFBlocks.ENRICHMENT_PLANT.get(), Kind.ENRICHMENT),
            Map.entry(TFBlocks.SATELLITE_LAUNCHPAD.get(), Kind.SATELLITE),
            Map.entry(TFBlocks.FUSION_REACTOR.get(), Kind.FUSION),
            Map.entry(TFBlocks.EV_CHARGER.get(), Kind.EV_CHARGER),
            Map.entry(TFBlocks.TELESCOPE.get(), Kind.TELESCOPE));

    private final Kind kind;

    public Era3MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Kind kind) {
        super(type, pos, state,
                2_000_000, 500_000, 0,   // буфер 2 МДж, приём 500 кДж/тик
                5_000, 2_000,              // теплоёмкость, макс K
                3_000, 5_000,              // макс RPM, момент
                4, 4,
                0, 0);
        this.kind = kind;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return switch (kind) {
            case SEMICONDUCTOR -> TFRecipeTypes.SEMICONDUCTOR_FURNACE.get();
            case TRANSISTOR -> TFRecipeTypes.TRANSISTOR_ASSEMBLER.get();
            case MICROCHIP -> TFRecipeTypes.MICROCHIP_FAB.get();
            case CARBON_FIBER -> TFRecipeTypes.CARBON_FIBER_PLANT.get();
            case REACTOR_CORE -> TFRecipeTypes.REACTOR_CORE.get();
            case ENRICHMENT -> TFRecipeTypes.ENRICHMENT_PLANT.get();
            case SATELLITE -> TFRecipeTypes.SATELLITE_LAUNCHPAD.get();
            case FUSION -> TFRecipeTypes.FUSION_REACTOR.get();
            case EV_CHARGER -> TFRecipeTypes.EV_CHARGER.get();
            case TELESCOPE -> TFRecipeTypes.TELESCOPE.get();
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + switch (kind) {
            case SEMICONDUCTOR -> "semiconductor_furnace";
            case TRANSISTOR -> "transistor_assembler";
            case MICROCHIP -> "microchip_fab";
            case CARBON_FIBER -> "carbon_fiber_plant";
            case REACTOR_CORE -> "reactor_core";
            case ENRICHMENT -> "enrichment_plant";
            case SATELLITE -> "satellite_launchpad";
            case FUSION -> "fusion_reactor";
            case EV_CHARGER -> "ev_charger";
            case TELESCOPE -> "telescope";
        });
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
