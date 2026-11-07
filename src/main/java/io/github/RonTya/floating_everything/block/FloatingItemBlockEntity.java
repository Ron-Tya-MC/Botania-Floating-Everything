package io.github.RonTya.floating_everything.block;

import io.github.RonTya.floating_everything.BasicRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.api.block.FloatingFlowerImpl;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.annotations.SoftImplement;

public class FloatingItemBlockEntity extends FunctionalFlowerBlockEntity implements ManaReceiver {
    private ItemStack displayedItem = ItemStack.EMPTY;
    private int cooldown = 0;
    private int manaDebt = 0;
    private int syncCooldown = 0;
    private static final int RANGE = 1;
    private static final int MAX_SYNC_COOLDOWN = 20;

    public static final String TAG_DISPLAYED_ITEM = "displayed_item";
    public static final String TAG_COOLDOWN = "cooldown";
    public static final String TAG_MANA_DEBT = "mana_debt";
    private static final String TAG_FLOATING_DATA = "floating";

    private final FloatingFlower floatingData = new FloatingFlowerImpl() {
        @NotNull
        @Override
        public ItemStack getDisplayStack() {
            return displayedItem;
        }
    };

    private final FloatingItemClicker.MiningProgress progress = new FloatingItemClicker.MiningProgress();

    public FloatingItemBlockEntity(BlockPos pos, BlockState state) {
        super(BasicRegister.FLOATING_ITEM_BE.get(), pos, state);

        setFloating(true);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        boolean reserveSync = false;

        if (!(level instanceof ServerLevel serverLevel) || displayedItem.isEmpty()) {
            return;
        }

        if (syncCooldown > 0) syncCooldown--;
        else {
            reserveSync = true;
            syncCooldown = MAX_SYNC_COOLDOWN;
        }

        FloatingItemClicker.ClickAction action = FloatingItemClicker.getAction(getFloatingData().getIslandType());
        int interval = action.intervalTicks();
        if (action.click() != FloatingItemClicker.ClickType.LEFT) {
            progress.clear();
        }

        if (cooldown > interval - 1) {
            cooldown = interval - 1;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (manaDebt > 0 || getMana() < FloatingItemCost.BASE_COST.get()) return;
        if (redstoneSignal != 0) return;
        if (!serverLevel.getChunkSource().hasChunk(worldPosition.getX() >> 4, worldPosition.getZ() >> 4)) return;

        FloatingItemClicker.ClickResult result = FloatingItemClicker.click(serverLevel, worldPosition, displayedItem, action.click(), progress);
        if (result.succeeded()) {
            addMana(-1 * FloatingItemCost.BASE_COST.get());
            if (result.consumedProportion() > 0.0f) {
                int extra = result.consumedProportion() >= 1.0f ? FloatingItemCost.CONSUME_COST.get() : Mth.floor(FloatingItemCost.DAMAGE_COST_RATE.get() * result.consumedProportion());
                int payment = Math.min(getMana(), extra);
                addMana(-1 * payment);
                manaDebt += extra - payment;
                if (extra >= FloatingItemCost.DAMAGE_COST_RATE.get() / 2) reserveSync = true;
            }
            setChanged();
            if (reserveSync) sync();
        }
        cooldown = interval - 1;
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }

    @Override
    public int getMaxMana() {
        return 4000;
    }

    @Override
    public int getColor() {
        return displayedItem.isEmpty() ? 0xffffff : displayedItem.getItem().hashCode() % 0xffffff;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    public CompoundTag createItemData() {
        CompoundTag data = new CompoundTag();
        if (!displayedItem.isEmpty()) {
            data.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        }
        if (manaDebt > 0) {
            data.putInt(TAG_MANA_DEBT, manaDebt);
        }
        if (cooldown > 0) {
            data.putInt(TAG_COOLDOWN, cooldown);
        }
        return data;
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);

        if (!displayedItem.isEmpty()) tag.put(TAG_DISPLAYED_ITEM, displayedItem.save(new CompoundTag()));
        tag.putInt(TAG_COOLDOWN, Math.max(cooldown, 0));
        tag.putInt(TAG_MANA_DEBT, Math.max(manaDebt, 0));
        tag.put(TAG_FLOATING_DATA, floatingData.writeNBT());
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);

        if (tag.contains(TAG_DISPLAYED_ITEM, Tag.TAG_COMPOUND)) displayedItem = ItemStack.of(tag.getCompound(TAG_DISPLAYED_ITEM)).copyWithCount(1);
        else displayedItem = ItemStack.EMPTY;

        cooldown = tag.getInt(TAG_COOLDOWN);
        manaDebt = tag.getInt(TAG_MANA_DEBT);

        progress.clear();

        FloatingFlower.IslandType oldType = floatingData.getIslandType();
        floatingData.readNBT(tag.getCompound(TAG_FLOATING_DATA));
        if (oldType != floatingData.getIslandType() && level != null && level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    private enum FloatingItemCost {
        BASE_COST(10),
        DAMAGE_COST_RATE(500),
        CONSUME_COST(1000);

        private final int cost;

        FloatingItemCost(int cost) {
            this.cost = cost;
        }

        public int get() {
            return cost;
        }
    }

    @Override
    public FloatingFlower getFloatingData() {
        return floatingData;
    }

    @SoftImplement("RenderDataBlockEntity")
    public Object getRenderData() {
        return floatingData.getIslandType();
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return getMana();
    }

    @Override
    public boolean isFull() {
        return manaDebt == 0 && getMana() >= getMaxMana();
    }

    @Override
    public void receiveMana(int amount) {
        if (amount <= 0) {
            return;
        }
        if (manaDebt > 0) {
            int payment = Math.min(manaDebt, amount);
            manaDebt -= payment;
            amount -= payment;
        }
        addMana(amount);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    public static class FloatingItemWandHud implements WandHUD {
        protected final FloatingItemBlockEntity flower;

        public FloatingItemWandHud(FloatingItemBlockEntity flower) {
            this.flower = flower;
        }

        public void renderHUD(GuiGraphics gui, Minecraft minecraft, int minLeft, int minRight, int minDown) {
            ItemStack displayed = flower.getFloatingData().getDisplayStack();
            String name = displayed.isEmpty() ? I18n.get(flower.getBlockState().getBlock().getDescriptionId() + ".empty")
                    : I18n.get(flower.getBlockState().getBlock().getDescriptionId(), displayed.getHoverName().getString());
            int color = flower.getColor();

            int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
            int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
            int left = (Math.max(102, minecraft.font.width(name)) + 4) / 2;

            int right = left + 20;

            left = Math.max(left, minLeft);
            right = Math.max(right, minRight);

            RenderHelper.renderHUDBox(gui, centerX - left, centerY + 8, centerX + right, centerY + Math.max(30, minDown));

            BotaniaAPIClient.instance().drawComplexManaHUD(gui, color, flower.getMana(), flower.getMaxMana(), name, flower.getHudIcon(), flower.isValidBinding());
        }

        @Override
        public void renderHUD(GuiGraphics gui, Minecraft mc) {
            renderHUD(gui, mc, 0, 0, 0);
        }
    }
}
