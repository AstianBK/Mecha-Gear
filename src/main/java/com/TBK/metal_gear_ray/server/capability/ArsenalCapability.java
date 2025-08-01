package com.TBK.metal_gear_ray.server.capability;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IArsenalPlayer;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class ArsenalCapability implements IArsenalPlayer {
    public Player player;
    public boolean rayActive = false;
    public CompoundTag data = new CompoundTag();

    public static ArsenalCapability get(Player player) {
        return MGCapability.getEntityCap(player, ArsenalCapability.class);
    }

    @Override
    public void setPlayer(Player player) {
        this.player=player;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public boolean rayActive(){
        return this.rayActive;
    }

    public void setRayActive(boolean f){
        this.rayActive = f;
    }
    @Override
    public void spawnOrCreateRay(Player player, BlockPos pos) {
        boolean isSpawn = false;
        MetalGearRayEntity ray=null;
        if(this.data.contains("isSummoning") && this.data.hasUUID("Owner")){
            MetalGearRayMod.LOGGER.debug("UUID :"+this.data.getUUID("Owner"));
            if(player.getUUID().equals(this.data.getUUID("Owner"))){
                ray = CVNEntityType.RAY.get().create(player.level());
                if(ray!=null){
                    ray.load(this.data);
                    ray.setPos(Vec3.atCenterOf(pos));
                    ray.setOwnerId(player.getUUID());
                    this.data.putBoolean("isSummoning",true);
                    this.rayActive = true;
                    player.level().addFreshEntity(ray);
                    isSpawn = true;
                }
            }
        }else {
            ray = CVNEntityType.RAY.get().create(player.level());
            if(ray!=null){
                ray.setOwnerId(player.getUUID());
                ray.setPos(Vec3.atCenterOf(pos));
                this.rayActive = player.level().addFreshEntity(ray);
                CompoundTag tag = new CompoundTag();
                ray.save(tag);
                tag.putBoolean("isSummoning",true);
                this.data = tag;
                isSpawn = true;
            }
        }
        if(isSpawn){
            if(!this.player.level().isClientSide){
                this.player.level().explode(ray,ray.getX(),ray.getY(),ray.getZ(),8.0F, Level.ExplosionInteraction.NONE);
            }
        }
    }

    public void copyFrom(ArsenalCapability capability){
        this.rayActive=capability.rayActive;
        this.data=capability.data;
    }

    public void saveRay(MetalGearRayEntity ray,Player player){
        CompoundTag tag = new CompoundTag();
        ray.save(tag);
        tag.putBoolean("isSummoning",false);
        this.rayActive=false;
        this.data=tag;
        ray.discard();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag=new CompoundTag();
        tag.putBoolean("ray_active",this.rayActive);
        tag.put("ray_data",this.data);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.rayActive = nbt.getBoolean("ray_active");
        this.data = nbt.getCompound("ray_data");
    }

    public static class ArsenalPlayerProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<IArsenalPlayer> instance = LazyOptional.of(ArsenalCapability::new);

        @NonNull
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return MGCapability.VAMPIRE_CAPABILITY.orEmpty(cap,instance.cast());
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
        }
    }
}
