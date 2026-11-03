package io.github.RonTya.floating_everything.client;

import io.github.RonTya.floating_everything.BasicRegister;
import io.github.RonTya.floating_everything.FloatingEverything;
import io.github.RonTya.floating_everything.block.FloatingItemBlockRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FloatingEverything.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BasicRegister.FLOATING_ITEM_BE.get(), FloatingItemBlockRenderer::new);
    }
}
