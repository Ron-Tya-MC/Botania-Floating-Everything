package io.github.RonTya.floating_everything.block;

import io.github.RonTya.floating_everything.BasicRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.block_entity.FloatingFlowerBlockEntity;

public class FloatingItemBlockEntity extends FloatingFlowerBlockEntity implements ManaReceiver {
    private ItemStack displayedItem = ItemStack.EMPTY;
    private int cooldown = 0;
    private int mana = 0;
    private int manaDebt = 0;
    private final static int MAX_MANA = 4000;

    public static final String TAG_DISPLAYED_ITEM = "displayed_item";
    public static final String TAG_COOLDOWN = "cooldown";
    public static final String TAG_MANA = "mana";
    public static final String TAG_MANA_DEBT = "mana_debt";

    private final FloatingItemClicker.MiningProgress progress = new FloatingItemClicker.MiningProgress();

    public FloatingItemBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @NotNull
    @Override
    public BlockEntityType<?> getType() {
        return BasicRegister.FLOATING_ITEM_BE.get();
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedItem = stack.copyWithCount(1);
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel) || displayedItem.isEmpty()) {
            return;
        }
        FloatingItemClicker.ClickAction action = FloatingItemClicker.getAction(getFloatingData().getIslandType());
        int interval = action.intervalTicks();
        if (action.click() != FloatingItemClicker.ClickType.LEFT) {
            progress.clear();
        }

        if (cooldown > interval - 1) {
            cooldown = interval - 1;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (manaDebt > 0 || mana < FloatingItemCost.BASE_COST.get()) {
            return;
        }
        if (!serverLevel.getChunkSource().hasChunk(worldPosition.getX() >> 4, worldPosition.getZ() >> 4)) {
            return;
        }

        FloatingItemClicker.ClickResult result = FloatingItemClicker.click(serverLevel, worldPosition, displayedItem, action.click(), progress);
        if (result.succeeded()) {
            mana -= FloatingItemCost.BASE_COST.get();
            if (result.consumedProportion() > 0.0f) {
                int extra = result.consumedProportion() >= 1.0f ? FloatingItemCost.CONSUME_COST.get() : Mth.floor(FloatingItemCost.DAMAGE_COST_RATE.get() * result.consumedProportion());
                int payment = Math.min(mana, extra);
                mana -= payment;
                manaDebt += extra - payment;
            }
            setChanged();
        }
        cooldown = interval - 1;
    }

    public CompoundTag createItemData() {
        CompoundTag data = new CompoundTag();
        if (!displayedItem.isEmpty()) {
            data.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        }
        if (manaDebt > 0) {
            data.putInt(TAG_MANA_DEBT, manaDebt);
        }
        if (cooldown > 0) {
            data.putInt(TAG_COOLDOWN, cooldown);
        }
        return data;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        if (!displayedItem.isEmpty()) tag.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        tag.putInt(TAG_COOLDOWN, Math.max(cooldown, 0));
        tag.putInt(TAG_MANA, Math.max(mana, 0));
        tag.putInt(TAG_MANA_DEBT, Math.max(manaDebt, 0));
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);
        if (tag.contains(TAG_DISPLAYED_ITEM, Tag.TAG_COMPOUND)) displayedItem = ItemStack.of(tag.getCompound(TAG_DISPLAYED_ITEM)).copyWithCount(1);
        else displayedItem = ItemStack.EMPTY;

        cooldown = tag.getInt(TAG_COOLDOWN);
        mana = tag.getInt(TAG_MANA);
        manaDebt = tag.getInt(TAG_MANA_DEBT);

        progress.clear();
    }

    private enum FloatingItemCost {
        BASE_COST(10),
        DAMAGE_COST_RATE(500),
        CONSUME_COST(1000);

        private final int cost;

        FloatingItemCost(int cost) {
            this.cost = cost;
        }

        public int get() {
            return cost;
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return manaDebt == 0 && mana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int amount) {
        if (amount <= 0) {
            return;
        }
        if (manaDebt > 0) {
            int payment = Math.min(manaDebt, amount);
            manaDebt -= payment;
            amount -= payment;
        }
        mana = Math.min(MAX_MANA, mana + amount);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }
}
