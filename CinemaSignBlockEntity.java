package com.example.cinemasign;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CinemaSignBlockEntity extends BlockEntity {
    private static final int MAX_TITLE = 32;
    private static final int CHECK_INTERVAL_TICKS = 10;

    @Nullable
    private BlockPos projector;
    private boolean playing;
    private String title = "";

    public CinemaSignBlockEntity(BlockPos pos, BlockState state) {
        super(CinemaSignMod.SIGN_BE.get(), pos, state);
    }

    @Nullable
    public BlockPos getProjector() {
        return projector;
    }

    public boolean hasProjector() {
        return projector != null;
    }

    public void setProjector(BlockPos pos) {
        this.projector = pos.immutable();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), UPDATE_ALL);
        }
    }

    private static final int UPDATE_ALL = 3;

    /** What the sign shows. */
    public Component displayText() {
        if (!playing) {
            return Component.literal("[CLOSED]");
        }
        String t = title.isBlank() ? "Untitled" : title;
        if (t.length() > MAX_TITLE) {
            t = t.substring(0, MAX_TITLE - 1) + "\u2026";
        }
        return Component.literal("[NOW SHOWING: " + t + "]");
    }

    public boolean isPlaying() {
        return playing;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CinemaSignBlockEntity be) {
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        boolean newPlaying = false;
        String newTitle = "";
        BlockPos p = be.projector;
        if (p != null && level.isLoaded(p) && CineBlocoBridge.isProjector(level, p)) {
            CineBlocoBridge.Status s = CineBlocoBridge.getStatus(level, p);
            newPlaying = s.playing();
            newTitle = s.title() == null ? "" : s.title();
        }
        if (newPlaying != be.playing || !newTitle.equals(be.title)) {
            be.playing = newPlaying;
            be.title = newTitle;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (projector != null) {
            tag.putLong("Projector", projector.asLong());
        }
        tag.putBoolean("Playing", playing);
        tag.putString("Title", title);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        projector = tag.contains("Projector") ? BlockPos.of(tag.getLong("Projector")) : null;
        playing = tag.getBoolean("Playing");
        title = tag.getString("Title");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
