package io.github.RonTya.floating_everything.item;

import io.github.RonTya.floating_everything.BasicRegister;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static io.github.RonTya.floating_everything.block.FloatingItemBlock.TAG_BLOCK_ENTITY_TAG;
import static io.github.RonTya.floating_everything.block.FloatingItemBlockEntity.TAG_DISPLAYED_ITEM;

public class FloatingItemBlockItem extends BlockItem {

    public FloatingItemBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        ItemStack displayed = getDisplayedItem(stack);
        if (displayed.isEmpty()) {
            return Component.translatable(getDescriptionId() + ".empty");
        }
        return Component.translatable(getDescriptionId(stack), displayed.getHoverName());
    }

    public static ItemStack create(ItemStack displayed) {
        ItemStack single = displayed.copy();
        single.setCount(1);
        CompoundTag data = new CompoundTag();
        data.put(TAG_DISPLAYED_ITEM, single.save(new CompoundTag()));
        ItemStack result = new ItemStack(BasicRegister.FLOATING_ITEM_ITEM.get());
        result.getOrCreateTag().put(TAG_BLOCK_ENTITY_TAG, data);
        return result;
    }

    public static ItemStack getDisplayedItem(ItemStack stack) {
        CompoundTag data = stack.getTagElement(TAG_BLOCK_ENTITY_TAG);
        if (data == null || !data.contains(TAG_DISPLAYED_ITEM, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(data.getCompound(TAG_DISPLAYED_ITEM));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new FloatingItemItemRenderer();
                }
                return renderer;
            }
        });
    }
}
