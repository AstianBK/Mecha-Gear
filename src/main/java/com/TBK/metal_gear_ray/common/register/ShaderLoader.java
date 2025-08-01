package com.TBK.metal_gear_ray.common.register;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "metal_gear_mod", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShaderLoader {
    public static ShaderInstance MASKED_SHADER;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation("metal_gear_mod", "interfaz_decal"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shaderInstance -> MASKED_SHADER = shaderInstance
        );
    }
}