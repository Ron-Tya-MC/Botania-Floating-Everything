package io.github.RonTya.floating_everything.block;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.RonTya.floating_everything.FloatingItemRenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.FloatingFlowerBlockEntityRenderer;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.Random;

public class FloatingItemBlockRenderer implements BlockEntityRenderer<FloatingItemBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FloatingItemBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(@NotNull FloatingItemBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FloatingFlowerBlockEntityRenderer.renderFloatingIsland(be, partialTick, poseStack, buffers, packedOverlay);

        ItemStack stack = be.getFloatingData().getDisplayStack();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        if (!BotaniaConfig.client().staticFloaters()) {
            applyFloatingTransform(be, partialTick, poseStack);
        }
        FloatingItemRenderHelper.renderDisplayedItem(itemRenderer, stack, poseStack, buffers,
                packedLight, packedOverlay, be.getLevel(), (int) be.getBlockPos().asLong());
        poseStack.popPose();
    }

    private static void applyFloatingTransform(FloatingItemBlockEntity be, float partialTick, PoseStack ms) {
        double worldTime = ClientTickHandler.ticksInGame + partialTick;
        if (be.getLevel() != null) {
            worldTime += new Random(be.getBlockPos().hashCode()).nextInt(1000);
        }

        ms.translate(0.5f, 0, 0.5f);
        ms.mulPose(VecHelper.rotateY(-((float) worldTime * 0.5f)));
        ms.translate(-0.5, (float) Math.sin(worldTime * 0.05f) * 0.1f, 0.5);

        ms.mulPose(VecHelper.rotateX(4f * (float) Math.sin(worldTime * 0.04f)));
        ms.mulPose(VecHelper.rotateY(90.0f));
    }
}
