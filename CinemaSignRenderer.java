package com.example.cinemasign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class CinemaSignRenderer implements BlockEntityRenderer<CinemaSignBlockEntity> {
    private final Font font;

    public CinemaSignRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(CinemaSignBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        Direction facing = be.getBlockState().getValue(CinemaSignBlock.FACING);
        Component text = be.displayText();
        int color = be.isPlaying() ? 0x55FF55 : 0xFF4040;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        // Front face of the panel is 2/16 in front of the block centre.
        pose.translate(0, 0, 0.125 + 0.003);

        float width = font.width(text);
        float scale = Math.min(0.02f, 0.9f / width);
        pose.scale(scale, -scale, scale);
        font.drawInBatch(text, -width / 2f, -font.lineHeight / 2f, color, false,
                pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }
}
