package io.github.RonTya.floating_everything;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.botania.common.lib.BotaniaTags;

public class FloatingItemRenderHelper {
    public static final int MAX_NEST_DEPTH = 8;
    private static int depth = 0;

    private FloatingItemRenderHelper() {}

    public static void renderDisplayedItem(ItemRenderer itemRenderer, ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, Level level, int seed) {
        if (stack.isEmpty() || depth >= MAX_NEST_DEPTH) {
            return;
        }

        float itemScale;
        float yOffSet;

        if (stack.is(BasicRegister.FLOATING_ITEM_ITEM.get()) || stack.is(BotaniaTags.Items.FLOATING_FLOWERS)) {
            itemScale = 1.5f;
            yOffSet = 0.9f;
        }
        else if (stack.getItem() instanceof BlockItem) {
            itemScale = 0.75f;
            yOffSet = 0.75f;
        }
        else {
            yOffSet = 0.76f;
            itemScale = 0.4f;
        }

        depth++;
        poseStack.pushPose();
        try {
            poseStack.translate(0.5F, yOffSet, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            poseStack.scale(itemScale, itemScale, itemScale);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffers, level, seed);
        } finally {
            poseStack.popPose();
            depth--;
        }
    }
}
