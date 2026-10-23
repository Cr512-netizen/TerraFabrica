package com.terrafabrica.content.era2;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Телефон (1×1×2): угольный микрофон + динамик + коммутатор. ПКМ по телефону
 * «звонит» ближайшему другому телефону в радиусе 100 блоков (документированное
 * упрощение: коммутатор выбирает ближайшего абонента).
 */
public class TelephoneBlockEntity extends AbstractMachineBlockEntity {

    private int ringCooldown;

    public TelephoneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 1_000, 400, 0, 0, 4, 4, 0, 0);
    }

    public void ring(Player caller) {
        if (ringCooldown > 0) return;
        ringCooldown = 60;
        // Ищем ближайший другой телефон и звоним ему.
        TelephoneBlockEntity nearest = null;
        double best = 100 * 100;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-50, -16, -50),
                worldPosition.offset(50, 16, 50))) {
            if (pos.equals(worldPosition)) continue;
            if (level.getBlockEntity(pos) instanceof TelephoneBlockEntity phone) {
                double d = pos.distSqr(worldPosition);
                if (d < best) {
                    best = d;
                    nearest = phone;
                }
            }
        }
        if (nearest != null) {
            nearest.ringCooldown = 60;
            nearest.getLevel().playSound(null, nearest.getBlockPos(),
                    SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BELL.value(),
                    SoundSource.BLOCKS, 0.6F, 0.8F);
        } else {
            caller.displayClientMessage(
                    Component.translatable("message.terrafabrica.phone_no_connection"), false);
        }
    }

    @Override
    protected void tickServer() {
        if (ringCooldown > 0) ringCooldown--;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.telephone");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
