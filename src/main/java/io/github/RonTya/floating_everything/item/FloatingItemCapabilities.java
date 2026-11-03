package io.github.RonTya.floating_everything.item;

import io.github.RonTya.floating_everything.FloatingEverything;
import io.github.RonTya.floating_everything.block.FloatingItemBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

@Mod.EventBusSubscriber(modid = FloatingEverything.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FloatingItemCapabilities {
    private static final ResourceLocation MANA_RECEIVER_ID = ResourceLocation.fromNamespaceAndPath(FloatingEverything.MOD_ID, "mana_receiver");

    private FloatingItemCapabilities() {}

//    @SubscribeEvent
//    public static void attachToBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
//        if (!(event.getObject() instanceof FloatingItemBlockEntity be)) {
//            return;
//        }
//        LazyOptional<ManaReceiver> receiver = LazyOptional.of(() -> be);
//        event.addCapability(MANA_RECEIVER_ID, new ICapabilityProvider() {
//            @NotNull
//            @Override
//            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
//                return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap, receiver);
//            }
//        });
//        event.addListener(receiver::invalidate);
//    }
}
