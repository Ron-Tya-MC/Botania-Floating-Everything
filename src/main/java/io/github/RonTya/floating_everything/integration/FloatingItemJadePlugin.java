package io.github.RonTya.floating_everything.integration;

import io.github.RonTya.floating_everything.FloatingEverything;
import io.github.RonTya.floating_everything.block.FloatingItemBlock;
import io.github.RonTya.floating_everything.block.FloatingItemBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

@WailaPlugin
public class FloatingItemJadePlugin implements IWailaPlugin {
    private static final ResourceLocation NAME_PROVIDER_ID = ResourceLocation.fromNamespaceAndPath(FloatingEverything.MOD_ID, "floating_item_name");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(NameProvider.INSTANCE, FloatingItemBlock.class);
    }

    private enum NameProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof FloatingItemBlockEntity be)) {
                return;
            }
            ItemStack displayed = be.getFloatingData().getDisplayStack();
            Component name = displayed.isEmpty() ? Component.translatable(accessor.getBlock().getDescriptionId() + ".empty") : Component.translatable(accessor.getBlock().getDescriptionId(), displayed.getHoverName());
            tooltip.remove(Identifiers.CORE_OBJECT_NAME);
            tooltip.add(0, IThemeHelper.get().title(name), Identifiers.CORE_OBJECT_NAME);
        }

        @Override
        public ResourceLocation getUid() {
            return NAME_PROVIDER_ID;
        }
    }
}
