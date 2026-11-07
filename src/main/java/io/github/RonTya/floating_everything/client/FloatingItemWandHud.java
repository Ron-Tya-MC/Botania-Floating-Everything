package io.github.RonTya.floating_everything.client;

import io.github.RonTya.floating_everything.block.FloatingItemBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.client.core.helper.RenderHelper;

public class FloatingItemWandHud implements WandHUD {
    private static final int MIN_HALF_WIDTH = 58;
    private static final int SIDE_PADDING = 5;
    private static final int ICON = 24;
    private static final int BOX_TOP_OFFSET = 5;
    private static final int BOX_HEIGHT = 25;

    private final FloatingItemBlockEntity be;

    public FloatingItemWandHud(@NotNull FloatingItemBlockEntity be) {
        this.be = be;
    }

    @Override
    public void renderHUD(@NotNull GuiGraphics gui, @NotNull Minecraft minecraft) {
        String title = makeTitle();

        int screenCenterX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int screenCenterY = minecraft.getWindow().getGuiScaledHeight() / 2;

        int halfTitleWidth = minecraft.font.width(title) / 2;
        int halfWidth = Math.max(MIN_HALF_WIDTH, halfTitleWidth + SIDE_PADDING);

        int boxLeft = screenCenterX - halfWidth;
        int boxRight = screenCenterX + halfWidth + ICON;
        int boxTop = screenCenterY + BOX_TOP_OFFSET;
        int boxBottom = boxTop + BOX_HEIGHT;

        RenderHelper.renderHUDBox(gui, boxLeft, boxTop, boxRight, boxBottom);

        BotaniaAPIClient.instance().drawComplexManaHUD(gui, be.getColor(), be.getMana(), be.getMaxMana(), title, be.getHudIcon(), be.isValidBinding());
    }

    private String makeTitle() {
        String descriptionId = be.getBlockState().getBlock().getDescriptionId();
        ItemStack displayed = be.getFloatingData().getDisplayStack();
        if (displayed.isEmpty()) {
            return I18n.get(descriptionId + ".empty");
        }
        return I18n.get(descriptionId, displayed.getHoverName().getString());
    }
}
