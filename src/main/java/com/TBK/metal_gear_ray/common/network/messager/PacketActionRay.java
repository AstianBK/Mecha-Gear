package com.TBK.metal_gear_ray.common.network.messager;

import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketActionRay implements Packet<PacketListener> {
    private final int x;
    private final int y;
    private final int z;
    private final int action;
    private final int idDragon;
    public PacketActionRay(FriendlyByteBuf buf) {
        this.x=buf.readInt();
        this.y=buf.readInt();
        this.z=buf.readInt();
        this.idDragon = buf.readInt();
        this.action = buf.readInt();
    }

    public PacketActionRay(int idDragon, int x, int y, int z,int action) {
        this.x=x;
        this.y=y;
        this.z=z;
        this.idDragon = idDragon;
        this.action = action;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.idDragon);
        buf.writeInt(this.action);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->{
            assert context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT;
            handlerAnim();
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handlerAnim() {
        Minecraft mc=Minecraft.getInstance();
        assert mc.level!=null;
        Entity dragon=mc.level.getEntity(this.idDragon);
        if(dragon instanceof MetalGearRayEntity ray){
            if(this.action==0){
                ray.setLaser(true);
            }
            ray.laserPosition=new Vec3(x,y,z);

        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
