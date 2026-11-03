package io.github.RonTya.floating_everything;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FloatingFakePlayer extends FakePlayer {
    private static final GameProfile PROFILE = new GameProfile(UUID.nameUUIDFromBytes("floating_everything:fake_player".getBytes(StandardCharsets.UTF_8)), "[Floating Everything] Floating Item Clicker");

    private static final Map<ServerLevel, FloatingFakePlayer> CACHED_FAKE_PLAYER = new HashMap<>();

    private FloatingFakePlayer(ServerLevel level) {
        super(level, PROFILE);
    }

    public static FloatingFakePlayer get(ServerLevel level) {
        return CACHED_FAKE_PLAYER.computeIfAbsent(level, FloatingFakePlayer::new);
    }

    public void refreshAttackStrength() {
        this.attackStrengthTicker = 10000;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CACHED_FAKE_PLAYER.clear();
    }
}
