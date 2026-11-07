package io.github.RonTya.floating_everything.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.RonTya.floating_everything.BasicRegister;
import io.github.RonTya.floating_everything.item.FloatingItemBlockItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.ArrayList;
import java.util.List;

public class FloatingItemRecipe implements RunicAltarRecipe {
    private final ResourceLocation id;
    private final int mana;

    public static final int MANA_USAGE = 10000;
    public static final int NEEDED_ITEMS = 8;

    private static final NonNullList<Ingredient> INGREDIENTS_DISPLAY = makeIngredients();
    private static final ItemStack RESULT_DISPLAY = FloatingItemBlockItem.create(new ItemStack(BasicRegister.EXAMPLE_ITEM.get()));;

    public FloatingItemRecipe(ResourceLocation id, int mana) {
        this.id = id;
        this.mana = mana;
    }

    @Override
    public boolean matches(@NotNull Container inv, @NotNull Level level) {
        return !getFloatableItem(inv).isEmpty();
    }

    private ItemStack getFloatableItem(Container inv) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        if (stacks.size() != NEEDED_ITEMS + 2) {
            return ItemStack.EMPTY;
        }

        for (int flower = 0; flower < stacks.size(); flower++) {
            if (!stacks.get(flower).is(BotaniaTags.Items.MUNDANE_FLOATING_FLOWERS)) {
                continue;
            }
            for (int redstone = 0; redstone < stacks.size(); redstone++) {
                if (redstone == flower || !stacks.get(redstone).is(BotaniaItems.redstoneRoot)) {
                    continue;
                }

                ItemStack candidate = ItemStack.EMPTY;
                boolean allSame = true;
                for (int i = 0; i < stacks.size(); i++) {
                    if (i == flower || i == redstone) {
                        continue;
                    }
                    ItemStack stack = stacks.get(i);
                    if (candidate.isEmpty()) {
                        candidate = stack;
                    } else if (!ItemStack.isSameItemSameTags(candidate, stack)) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) {
                    return candidate;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess registries) {
        ItemStack floatable = getFloatableItem(inv);
        if (floatable.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return FloatingItemBlockItem.create(floatable);
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registries) {
        return RESULT_DISPLAY;
    }

    @NotNull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return INGREDIENTS_DISPLAY;
    }

    private static NonNullList<Ingredient> makeIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(BotaniaTags.Items.MUNDANE_FLOATING_FLOWERS));
        list.add(Ingredient.of(BotaniaItems.redstoneRoot));
        for (int i = 0; i < NEEDED_ITEMS; i++) list.add(Ingredient.of(BasicRegister.EXAMPLE_ITEM.get()));
        return list;
    }

    @NotNull
    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BotaniaBlocks.runeAltar);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public int getManaUsage() {
        return mana;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return BasicRegister.FLOATING_ITEM_ALTAR_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<FloatingItemRecipe> {
        @NotNull
        @Override
        public FloatingItemRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            int mana = GsonHelper.getAsInt(json, "mana", MANA_USAGE);
            try {
                return new FloatingItemRecipe(id, mana);
            } catch (IllegalArgumentException e) {
                throw new JsonSyntaxException(e.getMessage(), e);
            }
        }

        @Override
        public FloatingItemRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf byteBuf) {
            int mana = byteBuf.readVarInt();
            return new FloatingItemRecipe(id, mana);
        }

        @Override
        public void toNetwork(FriendlyByteBuf byteBuf, FloatingItemRecipe recipe) {
            byteBuf.writeVarInt(recipe.getManaUsage());
        }
    }
}
