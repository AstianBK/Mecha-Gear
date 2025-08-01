package com.TBK.metal_gear_ray.common.network.messager;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class PacketKeySync implements Packet<PacketListener> {
    private final int key;


    public PacketKeySync(FriendlyByteBuf buf) {
        this.key=buf.readInt();
    }

    public PacketKeySync(int key) {
        this.key = key;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.key);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->{
            Player player = context.get().getSender();
            if(player!=null ){

                if(player.getVehicle()!=null){
                    if(this.key == 4){
                        ((MetalGearRayEntity)player.getVehicle()).knockBack(player.level().getEntitiesOfClass(Entity.class,player.getVehicle().getBoundingBox().inflate(10.0F), e->!player.getVehicle().is(e)),false);
                    } else if(this.key == 2){
                        player.getVehicle().setSprinting(true);
                    }else if(this.key == 3){
                        player.getVehicle().setSprinting(false);
                    }else if(player.getVehicle() instanceof IMecha mecha){
                        mecha.handleKey(this.key);
                    }
                }

            }

        });
        context.get().setPacketHandled(true);
    }



    @Override
    public void handle(PacketListener p_131342_) {

    }
}
