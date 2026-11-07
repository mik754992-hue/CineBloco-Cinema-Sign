package com.example.cinemasign;

import java.lang.reflect.Method;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The only class that talks to CineBloco (1.0.2-beta). It uses reflection, so this addon
 * compiles without CineBloco's jar and never ships any of its code.
 *
 * What it reads (all public methods in CineBloco):
 *   ScreenBlockEntity: type(), isOn(), isWallMember(), controller(), effectiveProgram()
 *   ScreenType:        hasScreen()
 *   MediaProgram:      hasMedia(), currentTitle(), clock()
 *   PlaybackClock:     playing()
 */
public final class CineBlocoBridge {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * false: sign says NOW SHOWING whenever the screen is on with something loaded (even if paused).
     * true:  sign says NOW SHOWING only while actually unpaused.
     */
    private static final boolean REQUIRE_UNPAUSED = false;

    public record Status(boolean playing, String title) {
        public static final Status IDLE = new Status(false, "");
    }

    private static boolean initDone;
    private static boolean available;
    private static Class<?> screenBeClass;
    private static Method mType, mHasScreen, mIsOn, mIsWallMember, mController, mEffectiveProgram,
            mHasMedia, mCurrentTitle, mClock, mClockPlaying;

    private static synchronized void init() {
        if (initDone) {
            return;
        }
        initDone = true;
        try {
            screenBeClass = Class.forName("dev.cinebloco.block.ScreenBlockEntity");
            Class<?> typeClass = Class.forName("dev.cinebloco.screen.ScreenType");
            Class<?> programClass = Class.forName("dev.cinebloco.screen.MediaProgram");
            Class<?> clockClass = Class.forName("dev.cinebloco.screen.PlaybackClock");

            mType = screenBeClass.getMethod("type");
            mIsOn = screenBeClass.getMethod("isOn");
            mIsWallMember = screenBeClass.getMethod("isWallMember");
            mController = screenBeClass.getMethod("controller");
            mEffectiveProgram = screenBeClass.getMethod("effectiveProgram");
            mHasScreen = typeClass.getMethod("hasScreen");
            mHasMedia = programClass.getMethod("hasMedia");
            mCurrentTitle = programClass.getMethod("currentTitle");
            mClock = programClass.getMethod("clock");
            mClockPlaying = clockClass.getMethod("playing");
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("[CinemaSign] Could not hook into CineBloco; signs will stay CLOSED.", e);
        }
    }

    /** The CineBloco screen block entity at pos if it is a projector or other picture screen, else null. */
    private static Object asScreen(BlockEntity be) {
        init();
        if (!available || be == null || !screenBeClass.isInstance(be)) {
            return null;
        }
        try {
            Object type = mType.invoke(be);
            if (type == null) {
                return null;
            }
            boolean projector = "PROJETOR".equals(((Enum<?>) type).name());
            boolean hasScreen = (Boolean) mHasScreen.invoke(type);
            return (projector || hasScreen) ? be : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /** True for CineBloco projectors (and TVs/monitors/other picture screens). */
    public static boolean isProjector(Level level, BlockPos pos) {
        return asScreen(level.getBlockEntity(pos)) != null;
    }

    public static Status getStatus(Level level, BlockPos pos) {
        Object be = asScreen(level.getBlockEntity(pos));
        if (be == null) {
            return Status.IDLE;
        }
        try {
            // Screens joined into a wall are driven by their controller.
            if ((Boolean) mIsWallMember.invoke(be)) {
                Object controller = mController.invoke(be);
                if (controller != null) {
                    be = controller;
                }
            }
            if (!(Boolean) mIsOn.invoke(be)) {
                return Status.IDLE;
            }
            Object program = mEffectiveProgram.invoke(be);
            if (program == null || !(Boolean) mHasMedia.invoke(program)) {
                return Status.IDLE;
            }
            if (REQUIRE_UNPAUSED) {
                Object clock = mClock.invoke(program);
                if (clock == null || !(Boolean) mClockPlaying.invoke(clock)) {
                    return Status.IDLE;
                }
            }
            Object title = mCurrentTitle.invoke(program);
            return new Status(true, title == null ? "" : title.toString());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Status.IDLE;
        }
    }

    private CineBlocoBridge() {}
}
