package com.TBK.metal_gear_ray.client.model;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.entity.MissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class MissileModel<T extends MissileEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(MetalGearRayMod.MODID, "raymissile"), "main");
	private final ModelPart truemain;
	private final ModelPart main;

	public MissileModel(ModelPart root) {
		this.truemain = root.getChild("truemain");
		this.main = this.truemain.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition truemain = partdefinition.addOrReplaceChild("truemain", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition main = truemain.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-75.5F, 135.25F, 59.3F, 4.0F, 4.0F, 25.0F, new CubeDeformation(0.0F))
		.texOffs(35, 3).addBox(-76.0F, 134.8F, 82.35F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(2, 31).addBox(-75.0F, 134.8F, 60.35F, 3.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(73.5F, -139.25F, -71.8F));

		PartDefinition leftarmlower_r1 = main.addOrReplaceChild("leftarmlower_r1", CubeListBuilder.create().texOffs(34, 11).addBox(-3.5F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-71.6197F, 137.25F, 80.494F, 0.0F, -1.1345F, 0.0F));

		PartDefinition rightarmlower1_r1 = main.addOrReplaceChild("rightarmlower1_r1", CubeListBuilder.create().texOffs(34, 18).addBox(-3.5F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-75.3803F, 137.25F, 80.494F, 0.0F, 1.1345F, 0.0F));

		PartDefinition leftarmlower13_r1 = main.addOrReplaceChild("leftarmlower13_r1", CubeListBuilder.create().texOffs(50, -1).addBox(0.0F, -2.5F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-73.5F, 137.3F, 87.325F, 0.0F, 0.0F, 0.7854F));

		PartDefinition leftarmlower12_r1 = main.addOrReplaceChild("leftarmlower12_r1", CubeListBuilder.create().texOffs(50, -1).addBox(0.0F, -2.5F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-73.5F, 137.3F, 87.325F, 0.0F, 0.0F, -0.7854F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		truemain.y= 1.8F;
		truemain.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}