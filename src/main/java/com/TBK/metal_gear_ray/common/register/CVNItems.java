package com.TBK.metal_gear_ray.common.register;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.items.DeployerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CVNItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MetalGearRayMod.MODID);



    public static final RegistryObject<Item> RAY_SPAWN_EGG = ITEMS.register("ray_spawn_egg",
            () -> new ForgeSpawnEggItem(CVNEntityType.RAY,0x948e8d, 0x573f2c,
                    new Item.Properties()));

    public static final RegistryObject<Item> DEPLOYER = ITEMS.register("ray_deployer",
            () -> new DeployerItem(new Item.Properties()));

}
