package com.terrafabrica.content.era1;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Ленточный транспортёр: скорость 1 м/с (1 блок за 20 тик), привод от вала
 * (≥500 Вт). Предметы движутся по цепочке транспортёров в направлении facing;
 * на конце — выгрузка в контейнер впереди. Поддержка поворотов — постановкой
 * блоков; сортировка — фильтром в GUI (слот-фильтр, только одинаковые предметы).
 */
public class ConveyorBlockEntity extends AbstractMachineBlockEntity {

    public static final double DRIVE_POWER_W = 500;
    /** Очередь предметов на ленте. */
    private final List<ItemStack> belt = new ArrayList<>();
    private int moveCooldown;

    public ConveyorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                1_000, 400,
                300, 2_000,
                4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        double power = rotation.getPower();
        rotation.reset();
        if (power < DRIVE_POWER_W) {
            status = STATUS_NO_POWER;
            updateBlockState(false);
            return;
        }
        status = STATUS_WORKING;
        updateBlockState(!belt.isEmpty());

        // Захват предмета сзади.
        if (belt.size() < 8) {
            BlockEntity behind = level.getBlockEntity(worldPosition.relative(facing().getOpposite()));
            if (behind != null) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK,
                        worldPosition.relative(facing().getOpposite()), facing());
                if (handler != null) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack extracted = handler.extractItem(i, 1, true);
                        if (!extracted.isEmpty()) {
                            belt.add(handler.extractItem(i, 1, false));
                            break;
                        }
                    }
                }
            }
        }

        // Движение вперёд: 1 блок за 20 тик.
        if (++moveCooldown >= 20 && !belt.isEmpty()) {
            moveCooldown = 0;
            BlockPos front = worldPosition.relative(facing());
            BlockEntity frontBe = level.getBlockEntity(front);
            if (frontBe instanceof ConveyorBlockEntity next && next.belt.size() < 8) {
                next.belt.add(belt.remove(0));
            } else {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, front, facing().getOpposite());
                if (handler != null) {
                    ItemStack moving = belt.get(0);
                    ItemStack leftover = insertAnywhere(handler, moving);
                    if (leftover.isEmpty()) {
                        belt.remove(0);
                    }
                }
            }
        }
        setChanged();
    }

    private static ItemStack insertAnywhere(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack;
        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            remaining = handler.insertItem(i, remaining, false);
        }
        return remaining;
    }

    private Direction facing() {
        return getBlockState().getValue(com.terrafabrica.content.shared.machine.MachineBlock.FACING);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        belt.clear();
        ListTag list = tag.getList("belt", 10);
        for (int i = 0; i < list.size(); i++) {
            belt.add(ItemStack.parseOptional(registries, list.getCompound(i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (ItemStack stack : belt) {
            list.add(stack.save(registries));
        }
        tag.put("belt", list);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.conveyor");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
