package com.TBK.metal_gear_ray.common.register;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CVNCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MetalGearRayMod.MODID);

    public static final RegistryObject<CreativeModeTab> BK_MOBS_TAB = TABS.register(MetalGearRayMod.MODID,()-> CreativeModeTab.builder()
            .icon(()->new ItemStack(CVNItems.DEPLOYER.get()))
            .title(Component.translatable("itemGroup.witch_archive"))
            .displayItems((s,a)-> {
                a.accept(new ItemStack(CVNItems.DEPLOYER.get()));
            })
            .build());
}
