package io.github.RonTya.floating_everything.client;

import io.github.RonTya.floating_everything.FloatingEverything;
import io.github.RonTya.floating_everything.block.FloatingItemBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;

@Mod.EventBusSubscriber(modid = FloatingEverything.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FloatingItemClientCapabilities {
    private static final ResourceLocation WAND_HUD_ID = ResourceLocation.fromNamespaceAndPath(FloatingEverything.MOD_ID, "wand_hud");

    private FloatingItemClientCapabilities() {}

    @SubscribeEvent
    public static void attachToBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof FloatingItemBlockEntity be)) {
            return;
        }
        LazyOptional<WandHUD> hud = LazyOptional.of(
                () -> new FloatingItemBlockEntity.FloatingItemWandHud(be));
        event.addCapability(WAND_HUD_ID, new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
                return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, hud);
            }
        });
        event.addListener(hud::invalidate);
    }
}