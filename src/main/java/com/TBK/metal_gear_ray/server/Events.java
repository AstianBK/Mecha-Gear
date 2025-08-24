package com.TBK.metal_gear_ray.server;


import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.server.capability.ArsenalCapability;
import com.TBK.metal_gear_ray.server.capability.MGCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MetalGearRayMod.MODID)

public class Events {


    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player player){
            ArsenalCapability oldCap = MGCapability.getEntityCap(event.getObject(), ArsenalCapability.class);

            if (oldCap == null) {
                ArsenalCapability.ArsenalPlayerProvider prov = new ArsenalCapability.ArsenalPlayerProvider();
                ArsenalCapability cap=prov.getCapability(MGCapability.ARSENAL_CAPABILITY).orElse(null);
                cap.setPlayer(player);
                event.addCapability(new ResourceLocation(MetalGearRayMod.MODID, "arsenal_cap"), prov);
            }
        }
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
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if(event.getItemStack().is(Items.STICK)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.x-=0.1D;
            }
            MetalGearRayMod.LOGGER.debug("X :" + MetalGearRayMod.x);
        }

        if(event.getItemStack().is(Items.BLAZE_ROD)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.y-=0.1D;
            }
            MetalGearRayMod.LOGGER.debug("Y :" + MetalGearRayMod.y);
        }
        if(event.getItemStack().is(Items.PRISMARINE_SHARD)){
            MetalGearRayMod.y+=0.1D;

            MetalGearRayMod.LOGGER.debug("Z :" + MetalGearRayMod.z);
        }

        if(event.getItemStack().is(Items.HEART_OF_THE_SEA)){
            if(event.getEntity().isShiftKeyDown()){
            }else {
                MetalGearRayMod.x+=0.1D;
            }
            MetalGearRayMod.LOGGER.debug("XQ :" + MetalGearRayMod.xq);
        }
        if(event.getItemStack().is(Items.GOLD_INGOT)){
            MetalGearRayMod.z+=0.1D;
        }

        if(event.getItemStack().is(Items.NETHERITE_INGOT)){
            MetalGearRayMod.z-=0.1D;
        }
        MetalGearRayMod.LOGGER.debug("X :" + MetalGearRayMod.x + " Y :"+MetalGearRayMod.y+
                " Z :" + MetalGearRayMod.z + " XQ :"+MetalGearRayMod.xq+
                " YQ :" + MetalGearRayMod.yq + " ZQ :"+MetalGearRayMod.zq);

    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event){
        if(event.getEntity().getVehicle() instanceof IMecha){
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if(Minecraft.getInstance().player!=null && Minecraft.getInstance().player.getVehicle() instanceof MetalGearRayEntity ray){
            event.setPitch(event.getPitch());
            event.setYaw(event.getYaw());
            if(!ray.isLaser() && !Minecraft.getInstance().options.getCameraType().isFirstPerson()){
                Minecraft.getInstance().gameRenderer.getMainCamera().move(-13 * ray.getCamInterpolation(Minecraft.getInstance().getPartialTick()), 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();

        oldPlayer.getCapability(MGCapability.ARSENAL_CAPABILITY).ifPresent(oldCap -> {
            newPlayer.getCapability(MGCapability.ARSENAL_CAPABILITY).ifPresent(newCap -> {
                newCap.copyFrom(oldCap);
            });
        });
    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderEvent(RenderLivingEvent.Pre<?,?> event){
        if(event.getEntity().isPassenger()){
            Entity mount = event.getEntity().getVehicle();
            if(mount instanceof MetalGearRayEntity ray){
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public static void seSalio(PlayerEvent.PlayerLoggedOutEvent event){
        if(event.getEntity().isPassenger() && event.getEntity().getVehicle() instanceof IMecha){
            event.getEntity().dismountTo(event.getEntity().getX(),event.getEntity().getY(),event.getEntity().getZ());
        }
    }
}
