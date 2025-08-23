package com.TBK.metal_gear_ray.client.renderer;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.client.model.MetalGearRayModel;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.renderable.CompositeRenderable;
import net.minecraftforge.client.model.renderable.ITextureRenderTypeLookup;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

public class EmissiveLayer<T extends MetalGearRayEntity, M extends MetalGearRayModel<T>> extends RenderLayer<T, M> {
    public final ResourceLocation GLOWING_PART = new ResourceLocation(MetalGearRayMod.MODID,"textures/entity/metal_gear_ray/ray_glowing.png");
    public EmissiveLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack p_117349_, MultiBufferSource p_117350_, int p_117351_, T p_117352_, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        this.getParentModel().renderToBuffer(p_117349_,p_117350_.getBuffer(RenderType.eyes(GLOWING_PART)), LivingEntityRenderer.getOverlayCoords(p_117352_,1.0F),OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);
    }

}
