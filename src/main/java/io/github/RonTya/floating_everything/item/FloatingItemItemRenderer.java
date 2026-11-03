package io.github.RonTya.floating_everything.item;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.RonTya.floating_everything.BasicRegister;
import io.github.RonTya.floating_everything.FloatingItemRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.block.FloatingFlowerImpl;
import vazkii.botania.forge.client.ForgeFloatingFlowerModel;

import static io.github.RonTya.floating_everything.block.FloatingItemBlock.TAG_BLOCK_ENTITY_TAG;

public class FloatingItemItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final java.lang.String TAG_FLOATING_DATA = "floating";

    public FloatingItemItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        renderIsland(minecraft, stack, poseStack, buffers, packedLight, packedOverlay);

        ItemStack displayed = FloatingItemBlockItem.getDisplayedItem(stack);
        FloatingItemRenderHelper.renderDisplayedItem(minecraft.getItemRenderer(), displayed, poseStack, buffers, packedLight, packedOverlay, minecraft.level, 0);
    }

    private static void renderIsland(Minecraft minecraft, ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FloatingFlowerImpl floating = new FloatingFlowerImpl();
        CompoundTag data = stack.getTagElement(TAG_BLOCK_ENTITY_TAG);
        if (data != null && data.contains(TAG_FLOATING_DATA, Tag.TAG_COMPOUND)) {
            floating.readNBT(data.getCompound(TAG_FLOATING_DATA));
        }
        ModelData modelData = ModelData.builder()
                .with(ForgeFloatingFlowerModel.FLOATING_PROPERTY, floating)
                .build();

        BlockState state = BasicRegister.FLOATING_ITEM.get().defaultBlockState();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(), modelData)) {
            dispatcher.getModelRenderer().renderModel(poseStack.last(),
                    buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                    state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, modelData, renderType);
        }
    }
}
