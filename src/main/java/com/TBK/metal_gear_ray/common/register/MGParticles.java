package com.TBK.metal_gear_ray.common.register;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MGParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MetalGearRayMod.MODID);

    public static final RegistryObject<SimpleParticleType> BEAM_EXPLOSION =
            PARTICLE_TYPES.register("beam_explosion", () -> new SimpleParticleType(true));
}
