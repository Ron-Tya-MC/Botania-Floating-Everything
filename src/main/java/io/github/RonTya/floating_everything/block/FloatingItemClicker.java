package io.github.RonTya.floating_everything.block;

import com.google.common.collect.Multimap;
import io.github.RonTya.floating_everything.FloatingEverything;
import io.github.RonTya.floating_everything.FloatingFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.block.FloatingFlower.IslandType;

import java.util.*;

public class FloatingItemClicker {
    public enum ClickType {
        RIGHT,
        SHIFT_RIGHT,
        LEFT
    }

    public static final ClickAction DEFAULT = new ClickAction(20, ClickType.RIGHT);
    private static final Map<IslandType, ClickAction> ACTIONS = new HashMap<>();

    static {
        ACTIONS.put(IslandType.SNOW, new ClickAction(40, ClickType.RIGHT));
        ACTIONS.put(IslandType.GRASS, new ClickAction(20, ClickType.RIGHT));
        ACTIONS.put(IslandType.PODZOL, new ClickAction(20, ClickType.SHIFT_RIGHT));
        ACTIONS.put(IslandType.MYCEL, new ClickAction(20, ClickType.LEFT));
        ACTIONS.put(IslandType.DRY, new ClickAction(10, ClickType.RIGHT));
        ACTIONS.put(IslandType.VIVID, new ClickAction(10, ClickType.SHIFT_RIGHT));
        ACTIONS.put(IslandType.GOLDEN, new ClickAction(10, ClickType.LEFT));
        ACTIONS.put(IslandType.SCORCHED, new ClickAction(1, ClickType.RIGHT));
        ACTIONS.put(IslandType.INFUSED, new ClickAction(1, ClickType.SHIFT_RIGHT));
        ACTIONS.put(IslandType.MUTATED, new ClickAction(1, ClickType.LEFT));
    }

    private FloatingItemClicker() {}

    public static ClickAction getAction(IslandType type) {
        return ACTIONS.getOrDefault(type, DEFAULT);
    }

    public static BlockPos targetPos(BlockPos floatingPos) {
        return floatingPos.below();
    }

    public static void click(ServerLevel level, BlockPos floatingPos, ItemStack displayedStack, ClickType clickType, MiningProgress progress) {
        FloatingFakePlayer player = FloatingFakePlayer.get(level);
        BlockPos target = targetPos(floatingPos);
        ItemStack heldStack = displayedStack.copyWithCount(1);
        Multimap<Attribute, AttributeModifier> modifiers = heldStack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        try {
            player.moveTo(floatingPos.getX() + 0.5, floatingPos.getY(), floatingPos.getZ() + 0.5, 0.0F, 90.0F);
            player.setOnGround(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, heldStack);
            player.setShiftKeyDown(clickType == ClickType.SHIFT_RIGHT);
            player.getAttributes().removeAttributeModifiers(modifiers);
            player.getAttributes().addTransientAttributeModifiers(modifiers);

            if (clickType == ClickType.LEFT) {
                leftClick(level, player, target, progress);
            } else {
                rightClick(level, player, target, heldStack);
            }
        } catch (Exception e) {
            FloatingEverything.LOGGER.error("[Botania: Floating Everything] Floating item action failed for {}", ForgeRegistries.ITEMS.getKey(displayedStack.getItem()), e);
        } finally {
            closeOpenedContainer(player);
            player.getAttributes().removeAttributeModifiers(modifiers);
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.getInventory().clearContent();
            player.getCooldowns().removeCooldown(displayedStack.getItem());
        }
    }

    private static void rightClick(ServerLevel level, FloatingFakePlayer player, BlockPos target, ItemStack held) {
        Entity entity = findTargetEntity(level, target, false);
        if (entity != null) {
            player.interactOn(entity, InteractionHand.MAIN_HAND);
            return;
        }
        Vec3 hitPos = new Vec3(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitPos, Direction.UP, target, false);
        player.gameMode.useItemOn(player, level, held, InteractionHand.MAIN_HAND, hit);
    }

    private static void leftClick(ServerLevel level, FloatingFakePlayer player, BlockPos target, MiningProgress miningProgress) {
        if (player.attackAnim == 0.0) player.swing(InteractionHand.MAIN_HAND);
        FloatingEverything.LOGGER.info("{}", player.attackAnim);

        Entity entity = findTargetEntity(level, target, true);
        if (entity != null) {
            clearCrack(level, player, target, miningProgress);
            player.refreshAttackStrength();
            player.attack(entity);
            return;
        }

        BlockState state = level.getBlockState(target);
        if (state.isAir()) {
            clearCrack(level, player, target, miningProgress);
            return;
        }

        if (miningProgress.state != state) {
            clearCrack(level, player, target, miningProgress);
            miningProgress.state = state;
        }

        float step = state.getDestroyProgress(player, level, target);
        if (step <= 0.0f) {
            return;
        }
        miningProgress.progress += step;
        if (miningProgress.progress >= 1.0f) {
            clearCrack(level, player, target, miningProgress);
            player.gameMode.destroyBlock(target);
        } else {
            level.destroyBlockProgress(player.getId(), target, (int) (miningProgress.progress * 10.0F));
        }
    }

    private static Entity findTargetEntity(ServerLevel level, BlockPos target, boolean onlyAttackable) {
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class, new AABB(target),
                e -> e.isAlive() && e.isPickable() && !e.isSpectator() && !(e instanceof Player)&& (!onlyAttackable || e.isAttackable()));
        Vec3 top = Vec3.atBottomCenterOf(target).add(0.0, 1.0, 0.0);
        Entity nearest = null;
        double nearestDistance = 2.0;
        for (Entity candidate : candidates) {
            double distance = candidate.getBoundingBox().getCenter().distanceToSqr(top);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static void closeOpenedContainer(FakePlayer player) {
        try {
            if (player.containerMenu != player.inventoryMenu) {
                player.doCloseContainer();
            }
        } catch (Exception e) {
            FloatingEverything.LOGGER.debug("Failed to close the container opened by the fake player", e);
        }
    }

    private static void clearCrack(ServerLevel level, FloatingFakePlayer player, BlockPos target,
                                   MiningProgress progress) {
        if (progress.state != null) {
            level.destroyBlockProgress(player.getId(), target, -1);
        }
        progress.clear();
    }

    public record ClickAction(int intervalTicks, ClickType click) {}

    public static final class MiningProgress {
        private BlockState state;
        private float progress;

        public void clear() {
            state = null;
            progress = 0.0f;
        }
    }
}
