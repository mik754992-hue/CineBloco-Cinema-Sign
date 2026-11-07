package com.example.cinemasign;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * The ONLY class that talks to CineBloco. Everything else in this addon is finished;
 * the two methods below need to be matched to CineBloco's real classes.
 *
 * CineBloco has no public API/docs, so these are placeholders:
 *  - isProjector(): guesses by block registry name (namespace + path contains "projector").
 *  - getStatus():   returns "idle" until you wire it to the real projector block entity.
 */
public final class CineBlocoBridge {
    // GUESSES: check the real ids in game with F3 while looking at a PeraProjector.
    private static final String CINEBLOCO_NAMESPACE = "cinebloco";
    private static final String PROJECTOR_PATH_HINT = "projector";

    public record Status(boolean playing, String title) {
        public static final Status IDLE = new Status(false, "");
    }

    public static boolean isProjector(Level level, BlockPos pos) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        return id.getNamespace().equals(CINEBLOCO_NAMESPACE) && id.getPath().contains(PROJECTOR_PATH_HINT);
    }

    public static Status getStatus(Level level, BlockPos projectorPos) {
        // TODO: replace with the real lookup, roughly:
        //   BlockEntity be = level.getBlockEntity(projectorPos);
        //   if (be instanceof <CineBloco projector block entity> p) {
        //       return new Status(p.<isPlaying>(), p.<currentTitleOrUrl>());
        //   }
        return Status.IDLE;
    }

    private CineBlocoBridge() {}
}
