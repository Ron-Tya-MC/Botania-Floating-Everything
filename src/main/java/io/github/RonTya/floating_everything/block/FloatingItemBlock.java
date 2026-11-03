package io.github.RonTya.floating_everything.block;

import io.github.RonTya.floating_everything.BasicRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;

public class FloatingItemBlock extends FloatingFlowerBlock {
    public static final String TAG_BLOCK_ENTITY_TAG = "BlockEntityTag";

    public FloatingItemBlock(BlockBehaviour.Properties p) {
        super(DyeColor.WHITE, p);
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FloatingItemBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != BasicRegister.FLOATING_ITEM_BE.get()) {
            return null;
        }
        BlockEntityTicker<FloatingItemBlockEntity> ticker = (level2, pos, state2, be) -> be.serverTick();
        return (BlockEntityTicker<T>) ticker;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        if (level.getBlockEntity(pos) instanceof FloatingItemBlockEntity be) {
            stack.getOrCreateTag().put(TAG_BLOCK_ENTITY_TAG, be.createItemData());
        }
        return stack;
    }
}
