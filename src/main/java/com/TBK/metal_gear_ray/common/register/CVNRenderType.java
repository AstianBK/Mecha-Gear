package com.TBK.metal_gear_ray.common.register;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class CVNRenderType extends RenderType {
    public CVNRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }


    public static RenderType getTremorzillaBeam(ResourceLocation locationIn, boolean irradiated) {
        return RenderType.create("tremorzilla_beam", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, CompositeState.builder()
                .setTextureState(new TextureStateShard(locationIn, false, false))
                .setShaderState(RenderType.RENDERTYPE_ENERGY_SWIRL_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOutputState(ITEM_ENTITY_TARGET)
                .createCompositeState(false));
    }

    public static RenderType getGhostCrumbling(ResourceLocation texture) {
        TextureStateShard lvt_1_1_ = new TextureStateShard(texture, false, false);
        return create("ghost_crumbling_am", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 262144, false, true, RenderType.CompositeState.builder().setTextureState(lvt_1_1_).setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).setDepthTestState(LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).createCompositeState(true));
    }
}
