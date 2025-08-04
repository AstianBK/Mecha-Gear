package com.TBK.metal_gear_ray.server.capability;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class MGCapability {
    public static final Capability<ArsenalCapability> ARSENAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ArsenalCapability.class);

    }

    @SuppressWarnings("unchecked")
    public static <T extends ArsenalCapability> T getEntityCap(Entity entity, Class<T> type) {
        if (entity != null) {
            ArsenalCapability entitypatch = entity.getCapability(MGCapability.ARSENAL_CAPABILITY).orElse(null);

            if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
                return (T)entitypatch;
            }
        }

        return null;
    }
}
