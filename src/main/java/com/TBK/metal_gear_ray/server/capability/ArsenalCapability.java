package com.TBK.metal_gear_ray.server.capability;

import com.TBK.metal_gear_ray.common.api.IArsenalPlayer;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;


public class ArsenalCapability implements IArsenalPlayer {
    public Player player;
    public BlockPos lastPos=null;
    public ResourceKey<Level> lastDimension=Level.OVERWORLD;
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

    public LivingEntity getRay(Level level){
        LivingEntity ray = null;
        if(this.data.hasUUID("UUID")){
            UUID uuid = this.data.getUUID("UUID");
            AABB area = new AABB(this.player.blockPosition()).inflate(16);

            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, area, e -> e.getUUID().equals(uuid));
            if(!list.isEmpty()){
                ray = list.get(0);
                if(ray!=null){
                    return ray;
                }
            }
            if(this.lastPos!=null && this.lastDimension!=null){
                if(level.dimension()==this.lastDimension){
                    ChunkPos chunkPos = new ChunkPos(this.lastPos);
                    this.refreshRayLocation(level,chunkPos);
                    AABB area1 = new AABB(lastPos).inflate(16);

                    List<LivingEntity> list1 = level.getEntitiesOfClass(LivingEntity.class, area1, e -> e.getUUID().equals(uuid));
                    if (!list1.isEmpty()) {
                        Entity entity = list1.get(0);
                        if(entity.isAlive()){
                            ray = (LivingEntity) entity;
                        }
                    }
                }else {
                    Level newLevel = level.getServer().getLevel(this.lastDimension);
                    if(newLevel==null)return null;
                    ChunkPos chunkPos = new ChunkPos(this.lastPos);
                    this.refreshRayLocation(level,chunkPos);
                    AABB area1 = new AABB(lastPos).inflate(16);

                    List<LivingEntity> list1 = newLevel.getEntitiesOfClass(LivingEntity.class, area1, e -> e.getUUID().equals(uuid));
                    if (!list1.isEmpty()) {
                        Entity entity = list1.get(0);
                        if(entity.isAlive()){
                            ray = (LivingEntity) entity;
                        }
                    }
                }
            }
        }
        return ray;
    }
    public void refreshRayLocation(Level level,ChunkPos chunkPos){
        level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
    }
    public void checkRayState(Level level){
        LivingEntity ray = getRay(level);
        if(ray==null){
            this.rayActive=false;
            this.data=new CompoundTag();
        }

    }
    public void rayDie(){
        this.rayActive=false;
        this.data = new CompoundTag();
    }
    @Override
    public void spawnOrCreateRay(Player player, BlockPos pos) {
        boolean isSpawn = false;
        MetalGearRayEntity ray=null;
        if(this.data.contains("isSummoning") && this.data.hasUUID("Owner")){
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
            if(!player.level().isClientSide){
                ray = new MetalGearRayEntity(CVNEntityType.RAY.get(),player.level());
                if(ray!=null){
                    ray.finalizeSpawn((ServerLevelAccessor) player.level(),player.level().getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED,null,null);
                    ray.setOwnerId(player.getUUID());
                    ray.setPos(Vec3.atCenterOf(pos));
                    player.level().addFreshEntity(ray);
                    this.rayActive = true;
                    CompoundTag tag = new CompoundTag();
                    ray.save(tag);
                    tag.putBoolean("isSummoning",true);
                    this.data = tag;
                    isSpawn = true;
                }
            }
        }
        if(isSpawn){
            if(!this.player.level().isClientSide){
                this.player.level().explode(ray,ray.getX(),ray.getY(),ray.getZ(),30.0F, Level.ExplosionInteraction.NONE);
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
        if(this.lastPos!=null){
            tag.put("lastPos",NbtUtils.writeBlockPos(this.lastPos));
        }

        tag.putString("dimension",this.lastDimension.location().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.rayActive = nbt.getBoolean("ray_active");
        this.data = nbt.getCompound("ray_data");
        if(nbt.contains("lastPos")){
            this.lastPos = NbtUtils.readBlockPos(nbt.getCompound("lastPos"));
        }
        this.lastDimension = ResourceKey.create(Registries.DIMENSION,new ResourceLocation(nbt.getString("dimension")));
    }

    public static class ArsenalPlayerProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<IArsenalPlayer> instance = LazyOptional.of(ArsenalCapability::new);

        @NonNull
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return MGCapability.ARSENAL_CAPABILITY.orEmpty(cap,instance.cast());
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
