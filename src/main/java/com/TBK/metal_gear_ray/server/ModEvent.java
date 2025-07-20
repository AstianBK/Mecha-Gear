package com.TBK.metal_gear_ray.server;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    public static void registerAttribute(EntityAttributeCreationEvent event) {
        event.put(CVNEntityType.RAY.get(), MetalGearRayEntity.setAttributes());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        //event.enqueueWork(BKItemProperties::register);
    }
}
