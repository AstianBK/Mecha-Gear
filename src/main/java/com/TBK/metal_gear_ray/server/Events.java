package com.TBK.metal_gear_ray.server;


import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID)

public class Events {



    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if(event.getItemStack().is(Items.STICK)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.x-=0.5D;
            }
                MetalGearRayMod.LOGGER.debug("X :" + MetalGearRayMod.x);
        }

        if(event.getItemStack().is(Items.BLAZE_ROD)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.y-=0.5D;
            }
            MetalGearRayMod.LOGGER.debug("Y :" + MetalGearRayMod.y);
        }
        if(event.getItemStack().is(Items.PRISMARINE_SHARD)){
            MetalGearRayMod.y+=0.5D;

            MetalGearRayMod.LOGGER.debug("Z :" + MetalGearRayMod.z);
        }

        if(event.getItemStack().is(Items.HEART_OF_THE_SEA)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.x+=0.5D;
            }
            MetalGearRayMod.LOGGER.debug("XQ :" + MetalGearRayMod.xq);
        }
        if(event.getItemStack().is(Items.FLINT)){
            if(event.getEntity().isShiftKeyDown()){
                MetalGearRayMod.yq+=0.5D;
            }else {
                MetalGearRayMod.yq-=0.5D;
            }
            MetalGearRayMod.LOGGER.debug("YQ :" + MetalGearRayMod.yq);
        }

        if(event.getItemStack().is(Items.AMETHYST_SHARD)){
            if(event.getEntity().isShiftKeyDown()){
                MetalGearRayMod.zq+=0.5D;
            }else {
                MetalGearRayMod.zq-=0.5D;
            }
            MetalGearRayMod.LOGGER.debug("ZQ :" + MetalGearRayMod.zq);
        }
        MetalGearRayMod.LOGGER.debug("X :" + MetalGearRayMod.x + " Y :"+MetalGearRayMod.y+
                " Z :" + MetalGearRayMod.z + " XQ :"+MetalGearRayMod.xq+
                " YQ :" + MetalGearRayMod.yq + " ZQ :"+MetalGearRayMod.zq);

    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderHandEvent(RenderHandEvent event){
        if(Minecraft.getInstance().player!=null){
            if(Minecraft.getInstance().player.isPassenger()){
                Entity mount = Minecraft.getInstance().player.getVehicle();
                if(mount instanceof IMecha){
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderEvent(RenderLivingEvent.Pre<?,?> event){
        if(event.getEntity().isPassenger()){
            Entity mount = event.getEntity().getVehicle();
            if(mount instanceof IMecha){
                event.setCanceled(true);
            }
        }
    }
}
