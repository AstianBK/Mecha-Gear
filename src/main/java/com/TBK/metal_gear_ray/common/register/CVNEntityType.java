package com.TBK.metal_gear_ray.common.register;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CVNEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MetalGearRayMod.MODID);

    public static final RegistryObject<EntityType<MetalGearRayEntity>> RAY =
            ENTITY_TYPES.register("ray",
                    () -> EntityType.Builder.of(MetalGearRayEntity::new, MobCategory.MONSTER)
                            .sized(1f, 1f)
                            .clientTrackingRange(40)
                            .build(new ResourceLocation(MetalGearRayMod.MODID, "ray").toString()));


}
