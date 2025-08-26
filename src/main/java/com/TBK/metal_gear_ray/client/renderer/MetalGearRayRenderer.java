package com.TBK.metal_gear_ray.client.renderer;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.client.model.MetalGearRayModel;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.register.CVNRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class MetalGearRayRenderer<T extends MetalGearRayEntity,M extends MetalGearRayModel<T>> extends MobRenderer<T,M> {
    public final ResourceLocation TEXTURE = new ResourceLocation(MetalGearRayMod.MODID,"textures/entity/metal_gear_ray/ray.png");
    private static final ResourceLocation BEAM_INNER_LOCATION = new ResourceLocation(MetalGearRayMod.MODID,"textures/entity/metal_gear_ray/beam/beam_inner.png");
    private static final ResourceLocation BEAM_OUTER_LOCATION = new ResourceLocation(MetalGearRayMod.MODID,"textures/entity/metal_gear_ray/beam/beam_outer.png");

    public MetalGearRayRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, (M) new MetalGearRayModel<>(p_174304_.bakeLayer(MetalGearRayModel.LAYER_LOCATION)), 0.0F);
        this.addLayer(new HeadModel<>(this));
        this.addLayer(new EmissiveLayer<>(this));
        this.shadowRadius = 4.0F;
    }

    @Override
    public boolean shouldRender(T p_115468_, Frustum p_115469_, double p_115470_, double p_115471_, double p_115472_) {
        if(super.shouldRender(p_115468_, p_115469_, p_115470_, p_115471_, p_115472_)){
            return true;
        }else {
            List<BlockPos> posList = p_115468_.crackingBlock.keySet().stream().toList();
            for (BlockPos pos : posList ){
                Vec3 vector3d = Vec3.atLowerCornerOf(pos);
                Vec3 vector3dCorner = Vec3.atLowerCornerOf(pos).add(1, 1, 1);
                if(p_115469_.isVisible(new AABB(vector3d.x, vector3d.y, vector3d.z, vector3dCorner.x, vector3dCorner.y, vector3dCorner.z))){
                    return true;
                }
            }
            Vec3 origin = p_115468_.getHeadPos();
            Vec3 end = p_115468_.laserPosition;
            return end!=null ? p_115469_.isVisible(new AABB(origin.x,origin.y,origin.z,end.x,end.y,end.z)) : false;
        }
    }

    @Nullable
    @Override
    protected RenderType getRenderType(T p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        return RenderType.entityTranslucent(this.getTextureLocation(p_115322_));
    }



    @Override
    public void render(T p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
        for(Map.Entry<BlockPos,Integer> entry : p_115455_.crackingBlock.entrySet()){
            p_115458_.pushPose();
            float progressMining = ((float)p_115455_.restoreCracking-entry.getValue())/200.0F;
            if(progressMining>=0.0F){
                BlockPos miningPos = entry.getKey();
                double d0 = Mth.lerp(p_115457_,p_115455_.xOld,p_115455_.getX());
                double d1 = Mth.lerp(p_115457_,p_115455_.yOld,p_115455_.getY());
                double d2 = Mth.lerp(p_115457_,p_115455_.zo,p_115455_.getZ());


                p_115458_.translate((double) miningPos.getX() - d0, (double) miningPos.getY() - d1, (double) miningPos.getZ() - d2);
                PoseStack.Pose posestack$pose = p_115458_.last();
                int progress = (int) Math.round((ModelBakery.DESTROY_TYPES.size() - 1) * (float) Mth.clamp(progressMining, 0F, 1.0F));

                VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), posestack$pose.pose(), posestack$pose.normal(), 1.0F);

                net.minecraftforge.client.model.data.ModelData modelData = p_115455_.level().getModelDataManager().getAt(miningPos);
                Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(p_115455_.level().getBlockState(miningPos), miningPos, p_115455_.level(), p_115458_, vertexconsumer1, modelData == null ? net.minecraftforge.client.model.data.ModelData.EMPTY : modelData);
            }
            p_115458_.popPose();
        }

        if(p_115455_.isLaser()){
            this.render(p_115458_,p_115459_,p_115455_,p_115457_);
        }
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, T jellyfish, float pPartialTicks) {
        float beamProgress = 1.0F;


        float ageInTicks = jellyfish.tickCount + pPartialTicks;
        float shakeByX = (float) Math.sin(ageInTicks * 4F) * 0.075F;
        float shakeByY = (float) Math.sin(ageInTicks * 4F + 1.2F) * 0.075F;
        float shakeByZ = (float) Math.sin(ageInTicks * 4F + 2.4F) * 0.075F;
        Vec3 beamOrigin = jellyfish.getHeadPos();
        Vec3 rawBeamPosition = jellyfish.getBeamDirection();
        float length = (float) rawBeamPosition.length();
        Vec3 vec3 = rawBeamPosition.normalize();
        float xRot = (float) Math.acos(vec3.y);
        float yRot = (float) Math.atan2(vec3.z, vec3.x);
        float width = beamProgress * 1.5F;

        Vec3 offSet = beamOrigin.subtract(jellyfish.position());
        pMatrixStack.pushPose();
        pMatrixStack.translate(
                shakeByX+offSet.x,
                shakeByY+offSet.y-1.0F,
                shakeByZ+offSet.z
        );

        pMatrixStack.mulPose(Axis.YP.rotationDegrees(((Mth.PI / 2F) - yRot) * Mth.RAD_TO_DEG));
        pMatrixStack.mulPose(Axis.XP.rotationDegrees((-(Mth.PI / 2F) + xRot) * Mth.RAD_TO_DEG));
        pMatrixStack.mulPose(Axis.ZP.rotationDegrees(45));


        renderBeam(jellyfish,pMatrixStack,pBuffer,pPartialTicks,1.5F,length,true,false);
        renderBeam(jellyfish,pMatrixStack,pBuffer,pPartialTicks,1.5F,length,false,false);

        pMatrixStack.popPose();
    }


    private void renderBeam(T entity, PoseStack poseStack, MultiBufferSource source, float partialTicks, float width, float length, boolean inner, boolean glowSecondPass) {
        poseStack.pushPose();
        int vertices;
        VertexConsumer vertexconsumer;
        float speed;
        float startAlpha = 1.0F;
        float endAlpha = 1.0F;
        if (inner) {
            vertices = 4;
            vertexconsumer = source.getBuffer(CVNRenderType.getBeam(BEAM_INNER_LOCATION,false));
            speed = 0.5F;
        } else {
            vertices = 8;
            vertexconsumer = source.getBuffer(CVNRenderType.getBeam(BEAM_OUTER_LOCATION,false));

            width += 0.25F;
            speed = 1F;
            endAlpha = 0.0F;
        }

        float v = ((float) entity.tickCount + partialTicks) * -0.25F * speed;
        float v1 = v + length * (inner ? 0.5F : 0.15F);
        float f4 = -width;
        float f5 = 0;
        float f6 = 0.0F;
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        for (int j = 0; j <= vertices; ++j) {
            Matrix3f matrix3f = posestack$pose.normal();
            float f7 = Mth.cos((float) Math.PI + (float) j * ((float) Math.PI * 2F) / (float) vertices) * width;
            float f8 = Mth.sin((float) Math.PI + (float) j * ((float) Math.PI * 2F) / (float) vertices) * width;
            float f9 = (float) j + 1;
            vertexconsumer.vertex(matrix4f, f4 * 0.55F, f5 * 0.55F, 0.0F).color(1.0F, 1.0F, 1.0F, startAlpha).uv(f6, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f4, f5, length).color(1.0F, 1.0F, 1.0F, endAlpha).uv(f6, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(matrix3f, 0.0F, -1F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8, length).color(1.0F, 1.0F, 1.0F, endAlpha).uv(f9, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(matrix3f, 0.0F, -1F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f7 * 0.55F, f8 * 0.55F, 0.0F).color(1.0F, 1.0F, 1.0F, startAlpha).uv(f9, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }
}
