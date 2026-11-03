package io.github.RonTya.floating_everything.block;

import io.github.RonTya.floating_everything.BasicRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.FloatingFlowerBlockEntity;

public class FloatingItemBlockEntity extends FloatingFlowerBlockEntity {
    private ItemStack displayedItem = ItemStack.EMPTY;
    private int cooldown = 0;

    public static final String TAG_DISPLAYED_ITEM = "displayed_item";

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
        if (!serverLevel.getChunkSource().hasChunk(worldPosition.getX() >> 4, worldPosition.getZ() >> 4)) {
            return;
        }

        FloatingItemClicker.click(serverLevel, worldPosition, displayedItem, action.click(), progress);
        cooldown = interval - 1;
    }

    public CompoundTag createItemData() {
        CompoundTag data = new CompoundTag();
        if (!displayedItem.isEmpty()) {
            data.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        }
        return data;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        if (!displayedItem.isEmpty()) {
            tag.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        }
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);
        if (tag.contains(TAG_DISPLAYED_ITEM, Tag.TAG_COMPOUND)) {
            displayedItem = ItemStack.of(tag.getCompound(TAG_DISPLAYED_ITEM)).copyWithCount(1);
        }
        else {
            displayedItem = ItemStack.EMPTY;
        }
    }
}
