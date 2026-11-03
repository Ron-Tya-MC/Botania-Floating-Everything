package io.github.RonTya.floating_everything;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(FloatingEverything.MOD_ID)
public class FloatingEverything {
    public static final String MOD_ID = "floating_everything";

    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public FloatingEverything() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BasicRegister.register(eventBus);
    }
}
