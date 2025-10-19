package com.TBK.metal_gear_ray.client;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.client.gui.ActionGui;
import com.TBK.metal_gear_ray.client.model.MetalGearRayModel;
import com.TBK.metal_gear_ray.client.model.MissileModel;
import com.TBK.metal_gear_ray.client.particles.BeamExplosionParticles;
import com.TBK.metal_gear_ray.common.register.MGParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientEvent {


    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MetalGearRayModel.LAYER_LOCATION,MetalGearRayModel::createBodyLayer);
        event.registerLayerDefinition(MissileModel.LAYER_LOCATION,MissileModel::createBodyLayer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @OnlyIn(Dist.CLIENT)
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        if(MGParticles.BEAM_EXPLOSION.isPresent()){
            event.registerSpriteSet(MGParticles.BEAM_EXPLOSION.get(), BeamExplosionParticles.Factory::new);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @OnlyIn(Dist.CLIENT)
    public static void registerArmorRenderers(EntityRenderersEvent.AddLayers event){

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @OnlyIn(Dist.CLIENT)
    public static void registerGui(RegisterGuiOverlaysEvent event){
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "overlay_ray",new ActionGui());
    }
}
