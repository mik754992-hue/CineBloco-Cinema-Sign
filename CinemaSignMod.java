package com.example.cinemasign;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CinemaSignMod.MODID)
public class CinemaSignMod {
    public static final String MODID = "cinemasign";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredBlock<CinemaSignBlock> CINEMA_SIGN = BLOCKS.register("cinema_sign",
            () -> new CinemaSignBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 6.0f)
                    .noOcclusion()
                    .lightLevel(state -> 7)));

    public static final DeferredItem<BlockItem> CINEMA_SIGN_ITEM =
            ITEMS.registerSimpleBlockItem("cinema_sign", CINEMA_SIGN);

    public static final DeferredItem<ProjectorLinkerItem> LINKER = ITEMS.register("projector_linker",
            () -> new ProjectorLinkerItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<BlockEntityType<CinemaSignBlockEntity>> SIGN_BE =
            BLOCK_ENTITIES.register("cinema_sign",
                    () -> BlockEntityType.Builder.of(CinemaSignBlockEntity::new, CINEMA_SIGN.get()).build(null));

    public CinemaSignMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CINEMA_SIGN_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(LINKER);
        }
    }
}
