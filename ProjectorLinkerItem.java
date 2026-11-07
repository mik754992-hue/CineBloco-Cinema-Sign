package com.example.cinemasign;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Sneak-click a projector to select it, then sneak-click a Cinema Sign to link it. */
public class ProjectorLinkerItem extends Item {
    private static final String KEY = "Projector";

    public ProjectorLinkerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        boolean isSign = level.getBlockState(pos).getBlock() instanceof CinemaSignBlock;
        boolean isProjector = CineBlocoBridge.isProjector(level, pos);
        if (!isSign && !isProjector) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide || player == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (isProjector) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(KEY, pos.asLong()));
            player.displayClientMessage(Component.literal("Projector selected. Now sneak-click a Cinema Sign."), true);
            return InteractionResult.CONSUME;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(KEY)) {
            player.displayClientMessage(Component.literal("Select a projector first (sneak-click it)."), true);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof CinemaSignBlockEntity be) {
            BlockPos target = BlockPos.of(tag.getLong(KEY));
            be.setProjector(target);
            player.displayClientMessage(Component.literal("Sign linked to projector at " + target.toShortString()), true);
        }
        return InteractionResult.CONSUME;
    }
}
