package com.TBK.metal_gear_ray.client.gui;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.register.CVNRenderType;
import com.TBK.metal_gear_ray.common.register.ShaderLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.Comparator;

public class ActionGui implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        assert player != null;
        if(mc.options.getCameraType().isFirstPerson()){
            if(player.isPassenger() && player.getVehicle() instanceof MetalGearRayEntity netheriteForge){
                guiGraphics.pose().pushPose();
                float cc = (((float)netheriteForge.cooldownLaser)/50.0F);
                printOverlay(guiGraphics,getGuiTextures(),screenWidth,screenHeight);

                if(netheriteForge.bladeOn()){
                    printOverlay(guiGraphics,getBladeState(),screenWidth,screenHeight);
                }
                if(netheriteForge.towerOn()){
                    printOverlay(guiGraphics,getTowerState(),screenWidth,screenHeight);
                }
                //printOverlay(guiGraphics,getLaserTextures(),i1,k1);
                this.printOverlayPercent(guiGraphics,getLaserTextures(),screenWidth,screenHeight,1.0F);
                this.printOverlayPercent(guiGraphics,getOffLaserTextures(),screenWidth,screenHeight,cc);
                guiGraphics.pose().popPose();
            }
        }
    }
    public void printOverlayPercent(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int screenWidth, int screenHeight, float percentHeight) {
        float scale = 0.33f;
        int spriteWidth = 103;
        int spriteHeight = 101;

        int drawX = (screenWidth / 2 - (int)(spriteWidth * scale / 2));
        int drawY = (screenHeight / 2 - (int)(spriteHeight * scale / 2)) +67;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(drawX, drawY, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.blit(resourceLocation, 0, 0,0,0,spriteWidth,  Mth.ceil(spriteHeight*percentHeight), spriteWidth, spriteHeight);
        guiGraphics.pose().popPose();
    }

    public void printOverlay(GuiGraphics guiGraphics,ResourceLocation resourceLocation,int screenWidth,int screenHeight){
        guiGraphics.pose().pushPose();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0,resourceLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, (double)screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double)screenWidth, (double)screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double)screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
    }
    public ResourceLocation getBladeState(){
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/ray_blade_on_overlay.png");
    }
    public ResourceLocation getTowerState(){
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/ray_turret_on_overlay.png");
    }
    public ResourceLocation getGuiTextures(){
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/ray_overlay.png");
    }
    public ResourceLocation getLaserTextures(){
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/ray_laser_on_overlay.png");
    }
    public ResourceLocation getOffLaserTextures(){
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/ray_laser_off_overlay.png");
    }
}
