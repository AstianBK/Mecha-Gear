package com.TBK.metal_gear_ray.server.keybind;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.event.KeyEvent;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID,bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MGKeybinds {
    public static KeyMapping attackKey2;
    public static KeyMapping attackKey3;
    public static KeyMapping attackKey4;

    @SubscribeEvent
    public static void register(final RegisterKeyMappingsEvent event) {
        attackKey2 = create("attack_key2", KeyEvent.VK_Z);
        attackKey3 = create("attack_key3", KeyEvent.VK_C);
        attackKey4 = create("attack_key4", KeyEvent.VK_R);

        event.register(attackKey2);
        event.register(attackKey3);
        event.register(attackKey4);

    }

    private static KeyMapping create(String name, int key) {
        return new KeyMapping("key." + MetalGearRayMod.MODID + "." + name, key, "key.category." +MetalGearRayMod.MODID);
    }
}
