package com.TBK.metal_gear_ray.common;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.network.PacketHandler;
import com.TBK.metal_gear_ray.common.network.messager.PacketKeySync;
import com.TBK.metal_gear_ray.server.keybind.MGKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeInputEvent {
    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        onInput(mc, event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        onInput(mc, event.getButton(), event.getAction());
    }

    private static void onInput(Minecraft mc, int key, int action) {
        if (mc.screen == null && (MGKeybinds.attackKey1.consumeClick() || MGKeybinds.attackKey2.consumeClick() ||MGKeybinds.attackKey3.consumeClick() ||MGKeybinds.attackKey4.consumeClick())) {
            PacketHandler.sendToServer(new PacketKeySync(key));
        }
    }
}
