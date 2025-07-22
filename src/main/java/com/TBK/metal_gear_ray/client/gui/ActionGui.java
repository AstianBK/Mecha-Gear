package com.TBK.metal_gear_ray.client.gui;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

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
        int originalX = 0;
        int originalY = 0;
        int spriteWidth = 103;
        int spriteHeight = 101;

        float drawX = (float) (( 406 + MetalGearRayMod.x + screenWidth / 2f));
        float drawY = (float) (( 266 + MetalGearRayMod.y + screenHeight));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.0f / 3.0F, 1.0F / 3.0F, 1.0F);

        guiGraphics.blit(resourceLocation,
                (int) drawX, (int) drawY,
                originalX, originalY,
                spriteWidth, Mth.floor(spriteHeight*percentHeight),
                spriteWidth, spriteHeight
        );

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
