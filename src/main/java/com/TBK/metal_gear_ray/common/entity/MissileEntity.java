package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import com.TBK.metal_gear_ray.common.register.CVNSounds;
import com.TBK.metal_gear_ray.common.register.MGParticles;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MissileEntity extends AbstractArrow {
    public int lifeTime = 0;
    public LivingEntity target = null;
    private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();
    private BlockState lastState;
    public double targetX = 0.0D;
    public double targetY = 0.0D;
    public double targetZ = 0.0D;
    public double targetXOld = 0.0D;
    public double targetYOld = 0.0D;
    public double targetZOld = 0.0D;
    public int maxTickAltura = 0;
    public double maxHeight = 0;
    public double distanceClient = 0.0D;
    public double distanceClientOld = 0.0D;
    public int delayTime = 0;
    public int lockTime = 0;
    public MissileEntity(Level p_37249_,int delayTime) {
        super(CVNEntityType.MISSILE.get(), p_37249_);
        this.setNoGravity(false);
        this.lifeTime = 50;
        this.delayTime = delayTime;
    }

    public MissileEntity(EntityType<MissileEntity> bulletEntityEntityType, Level level) {
        super(bulletEntityEntityType,level);
        this.lifeTime = 50;
    }

    @Override
    protected boolean canHitEntity(Entity p_36743_) {
        return super.canHitEntity(p_36743_) && (this.getOwner()==null || !this.getOwner().is(p_36743_));
    }

    @Override
    public boolean canBeHitByProjectile() {
        return super.canBeHitByProjectile();
    }

    public void setTarget(LivingEntity target){
        this.target = target;
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.targetZ = target.getZ();

        this.targetXOld = target.getX();
        this.targetYOld = target.getY();
        this.targetZOld = target.getZ();

        this.distanceClient = this.position().subtract(target.position()).length();
        this.distanceClientOld = this.distanceClient;
    }
    @Override
    protected void onHit(HitResult p_37260_) {
        HitResult.Type hitresult$type = p_37260_.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)p_37260_);
        }
        BlockPos end = BlockPos.containing(p_37260_.getLocation());
        if(this.level().isClientSide){
            for (int i = 0 ; i<3 ; i++){
                this.level().addParticle(MGParticles.BEAM_EXPLOSION.get(),end.getX()+this.random.nextInt(-2,2),end.getY()+this.random.nextInt(0,2),end.getZ()+this.random.nextInt(-2,2),0.0F,0.0F,0.0F);
            }
            this.level().playLocalSound(this.getTarget().blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL,20.0F,1.0f,false);
        }else {
            BeamExplosionEntity entity = this.createExplosion(end);
            this.level().getEntitiesOfClass(LivingEntity.class,new AABB(end).inflate(5.0F),e->!this.getOwner().is(e)).forEach(e->{
                e.invulnerableTime=0;
                e.hurt(damageSources().explosion(entity),10.0F);
                e.invulnerableTime=0;
            });
        }
        this.discard();
    }

    public BeamExplosionEntity createExplosion(BlockPos end){
        BeamExplosionEntity explosion = new BeamExplosionEntity(this.level(),this,null,new ExplosionDamageCalculator(),end.getX(),end.getY(),end.getZ(),4.0f,false, Explosion.BlockInteraction.DESTROY);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this.level(), explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(false);
        return explosion;
    }
    public Vec3 calculateJumpVelocity(BlockPos from, BlockPos to) {
        double g = 0.08; // gravedad aproximada de Minecraft

        // desplazamientos centrados
        double dx = (to.getX() + 0.5) - (from.getX() + 0.5);
        double dy = to.getY() - from.getY();
        double dz = (to.getZ() + 0.5) - (from.getZ() + 0.5);

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double vHoriz = 0.6;

        int ticks = 70;

        double vx = dx / ticks;
        double vz = dz / ticks;
        double vy = (dy + 0.5 * g * ticks * ticks) / ticks;

        int tickAltura = Mth.ceil(vy / g);
        this.maxHeight = (vy * vy) / (2 * g);
        this.maxTickAltura = tickAltura+this.tickCount+Mth.ceil(tickAltura*0.25F);

        return new Vec3(vx, vy, vz);
    }

    public Vec3 updateVelocity(Vec3 from, Vec3 to) {
        // desplazamientos centrados
        double dx = (to.x + 0.5) - (from.x + 0.5);
        double dz = (to.z + 0.5) - (from.z + 0.5);

        int ticks = Math.max(1,70-tickCount);
        double vx = dx / (ticks);
        double vz = dz / (ticks);

        return new Vec3(vx, this.getDeltaMovement().y, vz);
    }

    public void calculateRotPosition(BlockPos from, BlockPos to) {
        double g = 0.08;

        double dx = (to.getX() + 0.5) - (from.getX() + 0.5);
        double dy = to.getY() - from.getY();
        double dz = (to.getZ() + 0.5) - (from.getZ() + 0.5);


        int ticks = 70;

        double vx = dx / ticks;
        double vz = dz / ticks;
        double vy = (dy + 0.5 * g * ticks * ticks) / ticks;

        this.updateRot(new Vec3(vx,vy,vz),true);
    }
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public void tick() {
        boolean flag = this.isNoPhysics();
        this.targetXOld = this.targetX;
        this.targetYOld = this.targetY;
        this.targetZOld = this.targetZ;
        this.distanceClientOld = this.distanceClient;

        if(this.getTarget()!=null){
            Vec3 vec31 = this.getTarget().position();
            this.targetX = vec31.x;
            this.targetY = vec31.y;
            this.targetZ = vec31.z;

            this.distanceClient = vec31.subtract(this.position()).length();
        }
        if(this.level().isClientSide && this.getTarget()!=null && this.tickCount%6==0){
            this.level().playLocalSound(this.getTarget().blockPosition(), CVNSounds.RAY_MISSILE_LOCK.get(), SoundSource.NEUTRAL,3.0F,1.0f,false);
        }
        if(this.delayTime<=0){
            this.baseTick();

            if(this.getTarget()!=null){
                Vec3 start = this.position();
                Vec3 end = new Vec3(this.targetX,this.targetY,this.targetZ);
                Vec3 velocity = this.updateVelocity(start, end);
                this.setDeltaMovement(velocity);
            }

            Vec3 vec3 = this.getDeltaMovement();

            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (!blockstate.isAir() && !flag) {
                VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
                if (!voxelshape.isEmpty()) {
                    Vec3 vec31 = this.position();

                    for(AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockpos).contains(vec31)) {
                            this.inGround = true;
                            break;
                        }
                    }
                }
            }

            if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
                this.clearFire();
            }

            this.inGroundTime = 0;
            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.add(vec3);
            HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag) {
                    switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                        case SKIP_ENTITY:
                            if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                                this.onHit(hitresult);
                                this.hasImpulse = true;
                                break;
                            }
                            ignoredEntities.add(entityhitresult.getEntity().getId());
                            entityhitresult = null; // Don't process any further
                            break;
                        case STOP_AT_CURRENT_NO_DAMAGE:
                            this.discard();
                            entityhitresult = null; // Don't process any further
                            break;
                        case STOP_AT_CURRENT:
                            this.setPierceLevel((byte) 0);
                        case DEFAULT:
                            this.onHit(hitresult);
                            this.hasImpulse = true;
                            break;
                    }
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            if (this.isRemoved())
                return;

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            for(int i = 0; i < 1; ++i) {
                this.level().addParticle(ParticleTypes.POOF, this.getX() - d5, this.getY() - d6, this.getZ() - d1, -d5, -d6 + 0.2D, -d1);
            }
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
            }
            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            updateRot(vec3,flag);
            this.setPos(d7, d2, d3);

            this.checkInsideBlocks();
        }else {
            delayTime--;
            if(delayTime==0){
                BlockPos start = this.blockPosition();
                BlockPos end = target.blockPosition();
                Vec3 velocity = this.calculateJumpVelocity(start, end);

                this.setDeltaMovement(velocity);
                if(!this.level().isClientSide){
                    this.level().broadcastEntityEvent(this,(byte) 4);
                }
            }
        }
    }


    public void updateRot(Vec3 vec3,boolean flag){
        if (flag) {
            this.setYRot((float)(Mth.atan2(-vec3.x, -vec3.z) * (double)(180F / (float)Math.PI)));
        } else {
            this.setYRot(lerpRotation(this.getYRot(),(float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)),5.0F));
        }

        this.setXRot(lerpRotation(this.getXRot(),(float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)),5.0F));
    }

    private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
        float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);

        float clampedDelta = Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed);

        return currentYaw + clampedDelta;
    }
    private void startFalling() {
        this.inGround = false;
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
    }
    private boolean shouldFall() {
        return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.COPPER_HIT;
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        Entity entity = p_37259_.getEntity();
        if((this.getOwner() == null || !this.getOwner().is(entity))){
            entity.invulnerableTime=0;
            entity.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()),2.0F);
            entity.invulnerableTime=0;
        }
    }

    @Override
    public void handleEntityEvent(byte p_19882_) {
        if(p_19882_==4 && target!=null){
            BlockPos start = this.blockPosition();
            BlockPos end = target.blockPosition();
            this.calculateJumpVelocity(start, end);
        }
        super.handleEntityEvent(p_19882_);
    }

    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

}
