package com.TBK.metal_gear_ray.common.register;


import com.TBK.metal_gear_ray.MetalGearRayMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CVNSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MetalGearRayMod.MODID);

    //RAY
    public static final RegistryObject<SoundEvent> RAY_CHARGE_LASER=
            registerSoundEvent("ray_charge_laser");

    public static final RegistryObject<SoundEvent> RAY_SHOOT_LASER =
            registerSoundEvent("ray_shoot_laser");

    public static final RegistryObject<SoundEvent> RAY_ROAR =
            registerSoundEvent("ray_roar");


    public static final RegistryObject<SoundEvent> RAY_STOMP =
            registerSoundEvent("ray_stomp");

    public static final RegistryObject<SoundEvent> RAY_TURRET_SHOOT =
            registerSoundEvent("ray_turret_shoot");

    public static final RegistryObject<SoundEvent> RAY_FOOTSTEP =
            registerSoundEvent("ray_footstep");

    public static final RegistryObject<SoundEvent> RAY_JUMP =
            registerSoundEvent("ray_jump");

    public static RegistryObject<SoundEvent> registerSoundEvent(String name){
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MetalGearRayMod.MODID, name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
