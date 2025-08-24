package com.TBK.metal_gear_ray.client.renderer;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.client.model.MissileModel;
import com.TBK.metal_gear_ray.common.entity.MissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Map;

public class MissileRenderer<T extends MissileEntity> extends EntityRenderer<T> {
    public final MissileModel<T> missile;
    public final Map<Integer,ResourceLocation> TEXTURES = Map.of(0,new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/target_reticle/target_0.png"),
            1,new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/target_reticle/target_1.png"),
            2,new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/target_reticle/target_2.png"),
            3,new ResourceLocation(MetalGearRayMod.MODID,"textures/gui/target_reticle/target_3.png"));
    public MissileRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.missile = new MissileModel<>(p_174008_.bakeLayer(MissileModel.LAYER_LOCATION));
    }

    @Override
    public boolean shouldRender(T p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }

    public void render(T p_113839_, float p_113840_, float p_113841_, PoseStack p_113842_, MultiBufferSource p_113843_, int p_113844_) {
        super.render(p_113839_, p_113840_, p_113841_, p_113842_, p_113843_, p_113844_);
        p_113842_.pushPose();
        if(p_113839_.getTarget()!=null ){
            double d0 = Mth.lerp(p_113841_,p_113839_.xOld,p_113839_.getX());
            double d1 = Mth.lerp(p_113841_,p_113839_.yOld,p_113839_.getY());
            double d2 = Mth.lerp(p_113841_,p_113839_.zOld,p_113839_.getZ());

            double x = Mth.lerp(p_113841_,p_113839_.targetXOld,p_113839_.targetX);
            double y = Mth.lerp(p_113841_,p_113839_.targetYOld,p_113839_.targetY)+0.24D;
            double z = Mth.lerp(p_113841_,p_113839_.targetZOld,p_113839_.targetZ);
            double distance = Math.abs(Mth.lerp(p_113841_,p_113839_.yOld-p_113839_.targetYOld,p_113839_.getY()-p_113839_.targetY));
            int instancia = Mth.ceil(Mth.clamp(3.0D * (1.0F-(distance / Math.abs(p_113839_.maxHeight))),0,3));
            p_113842_.translate( x - d0,  y - d1,  z - d2);
            if(instancia<3){
                p_113842_.mulPose(Axis.YP.rotationDegrees(3.0F*p_113839_.tickCount));
            }
            PoseStack.Pose pose = p_113842_.last();
            this.drawSlash(pose,p_113839_,p_113843_,p_113844_,5.0F,p_113841_);
        }
        p_113842_.popPose();


        p_113842_.pushPose();
        p_113842_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_113841_,p_113839_.yRotO,p_113839_.getYRot())-90.0F));
        p_113842_.mulPose(Axis.XP.rotationDegrees(Mth.rotLerp(p_113841_,p_113839_.xRotO,p_113839_.getXRot())));
        //p_113842_.mulPose(Axis.ZP.rotationDegrees(45.0F));
        this.missile.renderToBuffer(p_113842_,p_113843_.getBuffer(RenderType.entityTranslucent(getTextureLocation(p_113839_))),p_113844_,OverlayTexture.NO_OVERLAY,1.0f,1.0f,1.0f,1.0f);
        p_113842_.popPose();
    }
    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return new ResourceLocation(MetalGearRayMod.MODID,"textures/entity/metal_gear_ray/ray_missile.png");
    }


    public ResourceLocation getTextureReticle(int index){
        return TEXTURES.get(index);
    }

    private void drawSlash(PoseStack.Pose pose, T entity, MultiBufferSource bufferSource, int light, float width,float partilTicks) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        double distance = Math.abs(Mth.lerp(partilTicks,entity.yOld-entity.targetYOld,entity.getY()-entity.targetY));
        int indexForDistance = entity.tickCount>entity.maxTickAltura && Math.abs(entity.maxHeight)>0 && entity.delayTime<=0? Mth.ceil(Mth.clamp(3.0D * (1.0F-(distance / Math.abs(entity.maxHeight))),0,3))  : 0;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(getTextureReticle(indexForDistance)));
        float halfWidth = width * .5f;
        consumer.vertex(poseMatrix, -halfWidth, -0.1f, -halfWidth).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, -0.1f, -halfWidth).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, -0.1f, halfWidth).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -halfWidth, -0.1f, halfWidth).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }
}
