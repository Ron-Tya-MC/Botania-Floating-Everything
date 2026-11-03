package io.github.RonTya.floating_everything;

import io.github.RonTya.floating_everything.block.FloatingItemBlock;
import io.github.RonTya.floating_everything.block.FloatingItemBlockEntity;
import io.github.RonTya.floating_everything.item.ExampleItem;
import io.github.RonTya.floating_everything.item.FloatingItemBlockItem;
import io.github.RonTya.floating_everything.recipe.FloatingItemRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BasicRegister {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FloatingEverything.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FloatingEverything.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FloatingEverything.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FloatingEverything.MOD_ID);

    public static final RegistryObject<Block> FLOATING_ITEM = BLOCKS.register("floating_item", () -> new FloatingItemBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.5f)
            .sound(SoundType.GRAVEL)
            .lightLevel(state -> 15)));

    public static final RegistryObject<Item> FLOATING_ITEM_ITEM = ITEMS.register("floating_item", () -> new FloatingItemBlockItem(FLOATING_ITEM.get(), new Item.Properties()));
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new ExampleItem(new Item.Properties()));

    public static final RegistryObject<BlockEntityType<FloatingItemBlockEntity>> FLOATING_ITEM_BE = BLOCK_ENTITY_TYPES.register("floating_item",
            () -> BlockEntityType.Builder.of(FloatingItemBlockEntity::new, FLOATING_ITEM.get()).build(null));

    public static final RegistryObject<RecipeSerializer<FloatingItemRecipe>> FLOATING_ITEM_ALTAR_SERIALIZER = RECIPE_SERIALIZERS.register("floating_items", FloatingItemRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
