package com.TBK.metal_gear_ray.server;


import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.server.capability.ArsenalCapability;
import com.TBK.metal_gear_ray.server.capability.MGCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
    @OnlyIn(Dist.CLIENT)
    public static void renderPlayer(RenderLivingEvent<?,?> event){
        if(event.getEntity() instanceof Player){
            if(event.getEntity().isPassenger()){
                Entity mount = event.getEntity().getVehicle();
                if(mount instanceof IMecha){
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.LAVA)) {
            event.setCanceled(true); // no recibe daño de fuego
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (Minecraft.getInstance().player.getVehicle() instanceof IMecha && event.getOverlay() == VanillaGuiOverlay.MOUNT_HEALTH.type()) {
            // Cancela la renderización de la barra de vida de la montura
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
