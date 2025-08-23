package com.TBK.metal_gear_ray.common.network.messager;

import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.entity.MissileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketMissileTarget implements Packet<PacketListener> {
    private final int idTarget;
    private final int idDragon;
    private final int delay;
    public PacketMissileTarget(FriendlyByteBuf buf) {
        this.idDragon = buf.readInt();
        this.idTarget = buf.readInt();
        this.delay = buf.readInt();
    }

    public PacketMissileTarget(int idDragon, int idTarget,int delay) {
        this.idDragon = idDragon;
        this.idTarget = idTarget;
        this.delay = delay;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.idDragon);
        buf.writeInt(this.idTarget);
        buf.writeInt(this.delay);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->{
            assert context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT;
            this.handlerAnim();
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handlerAnim() {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level!= null;
        if(mc.level.getEntity(this.idDragon) instanceof MissileEntity missile){
            if(mc.level.getEntity(this.idTarget) instanceof LivingEntity entity){
                missile.setTarget(entity);
                missile.delayTime = delay;
            }
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
