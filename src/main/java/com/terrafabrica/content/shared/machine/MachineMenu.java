package com.terrafabrica.content.shared.machine;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Меню GUI машин Terra Fabrica.
 *
 * <p>Сетка слотов фиксирована для всех машин: 4 входных + 4 выходных (машины
 * используют столько, сколько нужно; осознанное упрощение ради единого
 * клиентского конструктора — см. docs/DESIGN_DECISIONS.md). Данные (энергия,
 * прогресс, температура, RPM...) синхронизируются {@link ContainerData};
 * жидкости — пакетом {@code MenuFluidPayload}.
 */
public class MachineMenu extends AbstractContainerMenu {

    /** Число входных/выходных слотов (едино для всех машин). */
    public static final int INPUT_SLOTS = MachineLayout.INPUT_SLOTS;
    public static final int OUTPUT_SLOTS = MachineLayout.OUTPUT_SLOTS;
    public static final int SLOTS = MachineLayout.SLOTS;

    /** BE на сервере; {@code null} на клиенте (данные приходят пакетами). */
    protected final AbstractMachineBlockEntity machine;
    protected final ContainerData data;

    /** Флюиды баков на клиенте (обновляются MenuFluidPayload). */
    public final FluidStack[] clientFluids = new FluidStack[]{FluidStack.EMPTY, FluidStack.EMPTY};

    /** Серверный конструктор (вызывается из BE.createMenu). */
    public MachineMenu(int containerId, Inventory playerInventory,
                       AbstractMachineBlockEntity machine, ContainerData data) {
        this(MachineMenuTypeHolder.type(), containerId, playerInventory, machine, data);
    }

    /** Клиентский конструктор (вызывается MenuType). */
    public MachineMenu(int containerId, Inventory playerInventory) {
        this(MachineMenuTypeHolder.type(), containerId, playerInventory, null,
                new SimpleContainerData(AbstractMachineBlockEntity.DATA_SIZE));
    }

    protected MachineMenu(MenuType<?> type, int containerId, Inventory playerInventory,
                          AbstractMachineBlockEntity machine, ContainerData data) {
        super(type, containerId);
        this.machine = machine;
        this.data = data;

        if (machine != null) {
            addMachineSlots(machine.inventory());
        } else {
            // Клиент: слоты-заглушки в той же раскладке.
            for (int i = 0; i < SLOTS; i++) {
                addSlot(new Slot(new net.minecraft.world.SimpleContainer(SLOTS), i,
                        slotX(i), slotY(i)));
            }
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    protected void addMachineSlots(IItemHandler handler) {
        for (int i = 0; i < SLOTS; i++) {
            addSlot(new SlotItemHandler(handler, i, slotX(i), slotY(i)));
        }
    }

    /**
     * Раскладка слотов машины — в {@link MachineLayout}: слева два столбца входов (2x2),
     * справа два столбца выходов (2x2), между ними стрелка прогресса.
     * <p>
     * Прежняя раскладка (входы 44+18i и выходы 98+18i в одну строку) ставила вход №4 и выход №1
     * в одну и ту же точку (98, 35) и налезала на стрелку — это и был «поехавший» GUI. Теперь
     * координаты уникальны, что проверяет юнит-тест {@code MachineLayoutTest}.
     */
    protected int slotX(int index) {
        return MachineLayout.slotX(index);
    }

    protected int slotY(int index) {
        return MachineLayout.slotY(index);
    }

    protected void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        MachineLayout.INV_LEFT + col * MachineLayout.SLOT_SIZE,
                        MachineLayout.INV_ROW_Y + row * MachineLayout.SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    MachineLayout.INV_LEFT + col * MachineLayout.SLOT_SIZE, MachineLayout.HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (machine == null) {
            return true; // клиент: валидность определяет сервер (закрытие меню по пакету)
        }
        return machine.getLevel() != null
                && machine.getLevel().getBlockEntity(machine.getBlockPos()) == machine
                && player.distanceToSqr(machine.getBlockPos().getX() + 0.5,
                machine.getBlockPos().getY() + 0.5, machine.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int playerStart = SLOTS;
            int playerEnd = SLOTS + 36;

            if (index < SLOTS) {
                if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < playerEnd) {
                if (!moveItemStackTo(stack, 0, INPUT_SLOTS, false)) {
                    if (index < playerStart + 27) {
                        if (!moveItemStackTo(stack, playerStart + 27, playerEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(stack, playerStart, playerStart + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    public AbstractMachineBlockEntity machine() {
        return machine;
    }

    public ContainerData data() {
        return data;
    }

    /** Позволяет обойти проблему статической инициализации MenuType (см. TFMenus). */
    public static final class MachineMenuTypeHolder {
        /**
         * Держим не сам {@link MenuType}, а supplier: при инициализации мода
         * реестр меню ещё не заморожен и значение не связано, поэтому ранний
         * вызов {@code DeferredHolder#get()} падал бы с
         * «Trying to access unbound value».
         */
        private static java.util.function.Supplier<? extends MenuType<?>> supplier = () -> null;

        public static void bind(java.util.function.Supplier<? extends MenuType<?>> typeSupplier) {
            supplier = typeSupplier;
        }

        public static MenuType<?> type() {
            return supplier.get();
        }
    }
}
