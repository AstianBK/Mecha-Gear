package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.network.PacketHandler;
import com.TBK.metal_gear_ray.common.network.messager.PacketActionRay;
import com.TBK.metal_gear_ray.common.network.messager.PacketKeySync;
import com.TBK.metal_gear_ray.common.register.CVNSounds;
import com.TBK.metal_gear_ray.common.register.MGParticles;
import com.TBK.metal_gear_ray.server.keybind.MGKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MetalGearRayEntity extends PathfinderMob implements PlayerRideableJumping, RiderShieldingMount, IMecha {
    public static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LASER =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> TOWER_ON =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> BLADE_ON =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);

    public final ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator();
    public AnimationState stomp = new AnimationState();
    public AnimationState meleeAttack = new AnimationState();
    public AnimationState idle = new AnimationState();
    public AnimationState in_water = new AnimationState();
    public AnimationState blade_on = new AnimationState();
    public AnimationState blade_off = new AnimationState();
    public AnimationState tower_on = new AnimationState();
    public AnimationState tower_off = new AnimationState();
    public AnimationState prepare_laser = new AnimationState();
    public AnimationState laser = new AnimationState();
    private int idleAnimationTimeout;
    private int swimAnimationTimeout;
    private int attackTimer;
    private final TowerPart<?> tower0;
    private final TowerPart<?> tower1;
    private final TowerPart<?> tower2;
    private final TowerPart<?> tower3;
    private final TowerPart<?> head;
    private final TowerPart<?> body;
    private final TowerPart<?>[] towers;
    private final TowerPart<?>[] bodyParts;
    private final TowerPart<?>[] parts;
    public Vec3 laserPosition = Vec3.ZERO;
    public int prepareLaserTimer = 0;
    public int laserTimer = 0;
    public int resetTowerSoundTimer = 0;
    public float rotHeadY = 0.0F;
    public float rotHeadY0 = 0.0F;
    public float rotHeadX = 0.0F;
    public float rotHeadX0 = 0.0F;
    public boolean isJumping = false;
    public boolean isShooting = false;
    public float jumpPendingScale = 0.0F;
    public int cooldownLaser=0;
    public Map<BlockPos,Integer> crackingBlock = new HashMap<>();
    public int restoreCracking = 0;
    public int smokeEffectTimer = 0;
    public MetalGearRayEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.tower0=new TowerPart<>(this,"tower0",1,1);
        this.tower1=new TowerPart<>(this,"tower1",1,1);
        this.tower2=new TowerPart<>(this,"tower2",1,1);
        this.tower3=new TowerPart<>(this,"tower3",1,1);
        this.head = new TowerPart<>(this,"head",3,3);
        this.body = new TowerPart<>(this,"body",10,10);

        this.towers = new TowerPart[]{this.tower0,this.tower1,this.tower2,this.tower3};
        this.bodyParts = new TowerPart[]{this.body,this.head};
        this.parts = new TowerPart[]{this.tower0,this.tower1,this.tower2,this.tower3,this.body,this.head};
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
    }

    @Override
    public void setId(int p_20235_) {
        super.setId(p_20235_);
        for (int i = 0; i < this.parts.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.parts[i].setId(p_20235_ + i + 1);
    }

    public static AttributeSupplier setAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR,10.0D)
                .add(Attributes.ARMOR_TOUGHNESS,5.0D)
                .add(Attributes.FOLLOW_RANGE, 45.D)
                .add(Attributes.MOVEMENT_SPEED, 0.1d)
                .add(Attributes.ATTACK_DAMAGE,12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F)
                .add(Attributes.JUMP_STRENGTH,0.42F)
                .build();

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3,new RayAttackGoal(this,2.0D,false));
        this.targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(this,LivingEntity.class,false));
    }

    public Vec3 getHeadPos(){
        double x = this.head.getX();
        double y = this.head.getY();
        double z = this.head.getZ();

        return new Vec3(x,y,z);
    }

    public Vec3 getBeamDirection(){
        Vec3 direction;
        if(this.isVehicle()){
            direction = this.getHeadPos().add(this.getControllingPassenger().getLookAngle().scale(50.0D)).subtract(this.getHeadPos().add(this.getControllingPassenger().getLookAngle().scale(0.5D)));
        }else {
            direction = this.getHeadPos().add(this.viewHeadY().scale(50.0D)).subtract(this.getHeadPos().add(this.viewHeadY().scale(0.5D)));
        }
        return direction;
    }

    protected void executeRidersJump(float p_251967_, Vec3 p_275627_) {
        double d0 = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.getBlockJumpFactor() + (double)this.getJumpBoostPower();
        this.addDeltaMovement(this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)(22.2222F * p_251967_) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0D, (double)(4.4285F * p_251967_) * d0, 0.0D));
        this.hasImpulse = true;
    }

    protected void tickRidden(Player p_278233_, Vec3 p_275693_) {
        super.tickRidden(p_278233_, p_275693_);
        if (this.isControlledByLocalInstance()) {
            if (p_275693_.z <= 0.0D) {
                //this.gallopSoundCounter = 0;
            }

            if (this.onGround()) {
                this.setIsJumping(false);
                if (this.jumpPendingScale>0.0F && !this.isJumping()) {
                    this.executeRidersJump(1.0F, p_275693_);
                }

                this.jumpPendingScale = 0.0F;
            }
        }

    }

    public void setIsJumping(boolean isJumping){
        this.isJumping = isJumping;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    @Override
    public boolean hurt(DamageSource p_21016_, float p_21017_) {
        if(this.getPassengers().contains(p_21016_.getEntity())){
            return false;
        }
        return super.hurt(p_21016_, p_21017_);
    }

    @Override
    public void tick() {
        super.tick();

        this.refreshDimensions();

        if(!this.isVehicle()){
            if(this.level().getEntities(this,this.body.getBoundingBox().inflate(3.0F),EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e->!this.is(e))).isEmpty()){
                this.setTowerOn(false);
                if(!this.level().isClientSide){
                    this.level().broadcastEntityEvent(this,(byte) 9);
                }
                this.resetTowerSoundTimer=0;
            }else {
                this.setTowerOn(true);
                if(!this.level().isClientSide){
                    this.level().broadcastEntityEvent(this,(byte) 12);
                }
            }
        }

        if(this.towerOn()){
            for (TowerPart<?> leg : this.towers) {
                leg.tick();
                leg.setSize(EntityDimensions.scalable(1,1));
            }
        }else{
            for (TowerPart<?> leg : this.towers) {
                leg.tick();
                leg.setSize(EntityDimensions.scalable(0, 0));
            }
        }

        float yawRad = (float)Math.toRadians(this.getYRot());
        float sin = (float)Math.sin(yawRad);
        float cos = (float)Math.cos(yawRad);

        double body0X =  (-7 * sin);
        double body0Z = -(-7 * cos);

        Vec3[] pos = new Vec3[this.bodyParts.length];
        for (int j = 0 ; j<this.bodyParts.length ; j++){
            pos[j]=new Vec3(this.bodyParts[j].getX(), this.bodyParts[j].getY(), this.bodyParts[j].getZ());
        }

        this.tickPart(this.head,body0X,7,body0Z);
        this.tickPart(this.body,0,0,0);

        for (int k = 0 ; k<this.bodyParts.length ; k++){
            this.bodyParts[k].xo = pos[k].x;
            this.bodyParts[k].yo = pos[k].y;
            this.bodyParts[k].zo = pos[k].z;
            this.bodyParts[k].xOld = pos[k].x;
            this.bodyParts[k].yOld = pos[k].y;
            this.bodyParts[k].zOld = pos[k].z;
        }

        if(this.towerOn()){
            Vec3[] avec3 = new Vec3[this.towers.length];

            for(int j = 0; j < this.towers.length; ++j) {
                avec3[j] = new Vec3(this.towers[j].getX(), this.towers[j].getY(), this.towers[j].getZ());
            }

            // Rotación enY ( horizontal)

            // torre 1: adelante derecha
            double leg0X =  ( 2 * cos) + ( -4 * sin);
            double leg0Z =  ( 2 * sin) - ( -4 * cos);

            // torre 2: atrás izquierda
            double leg1X =  ( -2 * cos) + ( -4 * sin);
            double leg1Z =  ( -2 * sin) - ( -4 * cos);

            // torre 3: atrás derecha
            double leg2X =  - ( 4 * cos);
            double leg2Z =  - ( 4 * sin);


            double leg3X =  - ( -4 * cos);
            double leg3Z =  - ( -4 * sin);


            this.tickPart(this.tower0,leg0X, 9,leg0Z);
            this.tickPart(this.tower1,leg1X,9,leg1Z);
            this.tickPart(this.tower2,leg2X,8,leg2Z);
            this.tickPart(this.tower3,leg3X,8,leg3Z);



            for(int l = 0; l < this.towers.length; ++l) {
                this.towers[l].xo = avec3[l].x;
                this.towers[l].yo = avec3[l].y;
                this.towers[l].zo = avec3[l].z;
                this.towers[l].xOld = avec3[l].x;
                this.towers[l].yOld = avec3[l].y;
                this.towers[l].zOld = avec3[l].z;
            }

            if(!this.level().isClientSide){
                if(this.isShooting && this.resetTowerSoundTimer++>15){
                    this.level().playSound(null,this,CVNSounds.RAY_TURRET_SHOOT.get(),SoundSource.NEUTRAL,1.0F,1.0F);
                    this.resetTowerSoundTimer=0;
                }
            }

            if(!this.isVehicle()){
                this.checkTick();
            }else {
                this.towerTick();
            }
        }

        if(this.prepareLaserTimer>0){
            this.prepareLaserTimer--;
            this.getNavigation().stop();
            if (!this.level().isClientSide && this.getTarget()!=null){
                this.setPos(this.position());

                Vec3 vec32 = this.getTarget().position().subtract(this.getEyePosition());
                double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
                double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
                this.yHeadRot=(float)f6;
                this.setYHeadRot((float) f6);
                this.yBodyRot= (float) (f6);
                this.setYRot((float) f6);
                this.setXRot((float) f5);
                this.setRot(this.getYRot(),this.getXRot());
                this.rotHeadX = lerpRotation(this.rotHeadX,(float)f5 ,5.0F);

                this.rotHeadY = Mth.clamp(lerpRotation(this.rotHeadY,(float) f6-this.getYRot(),5.0F),-60,60);

            }
            if(this.prepareLaserTimer==0){
                if(!this.level().isClientSide){
                    this.setLaser(true);
                    if(this.isVehicle()){
                        this.laserPosition=null;
                        this.level().broadcastEntityEvent(this,(byte) 14);
                    }else if(this.getTarget()!=null){
                        this.laserPosition = this.getTarget().position();
                        PacketHandler.sendToAllTracking(new PacketActionRay(this.getId(), (int) this.getTarget().getX(), (int) this.getTarget().getY(), (int) this.getTarget().getZ()),this);
                    }
                }else {
                    this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_SHOOT_LASER.get(),SoundSource.NEUTRAL,3.0F,1.0F+3.0F*this.random.nextFloat(),false);
                    this.idleAnimationTimeout = 200;
                    this.prepare_laser.stop();
                    this.laser.start(this.tickCount);
                }
            }
        }

        if(this.cooldownLaser>0){
            this.cooldownLaser--;
        }

        if(this.isLaser()){
            this.laserTimer--;
            this.getNavigation().stop();
            if(this.isVehicle()){
                if(this.tickCount%5 == 0 && this.getControllingPassenger()!=null){
                    BlockHitResult blockEnd = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.getControllingPassenger().getLookAngle().scale(50.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
                    List<EntityHitResult> hits = this.getBeamEntityHitResult(this.level(),this,this.getHeadPos(),this.getHeadPos().add(this.getControllingPassenger().getLookAngle().scale(50.0D)),this.getBoundingBox().inflate(100.0F), e->!this.is(e),0.5F);
                    if(hits!=null){
                        for (EntityHitResult hit : hits){
                            if(hit.getEntity() instanceof LivingEntity){
                                LivingEntity entity = (LivingEntity) hit.getEntity();
                                if(entity.hurt(this.damageSources().generic(),3.0F)){
                                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,3,10));
                                }
                            }
                        }
                    }else if(!level().getBlockState(blockEnd.getBlockPos()).isAir()){
                        BlockPos end = blockEnd.getBlockPos();

                        if(this.level().isClientSide){
                            for (int i = 0 ; i<3 ; i++){
                                this.level().addParticle(MGParticles.BEAM_EXPLOSION.get(),end.getX()+this.random.nextInt(-2,2),end.getY()+this.random.nextInt(0,2),end.getZ()+this.random.nextInt(-2,2),0.0F,0.0F,0.0F);
                            }
                        }else {
                            this.createExplosion(end);
                        }
                    }
                }
            }else {
                if(this.tickCount%5 == 0){
                    BlockHitResult blockEnd = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.viewHeadY().scale(50.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
                    List<EntityHitResult> hits = this.getBeamEntityHitResult(this.level(),this,this.getHeadPos(),this.getHeadPos().add(this.viewHeadY().scale(50.0D)),this.getBoundingBox().inflate(100.0F), e->!this.is(e),0.5F);
                    if(hits!=null){
                        for (EntityHitResult hit : hits){
                            if(hit.getEntity() instanceof LivingEntity){
                                LivingEntity entity = (LivingEntity) hit.getEntity();
                                if(entity.hurt(this.damageSources().generic(),3.0F)){
                                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,3,10));
                                }
                            }
                        }
                    }else if(!level().getBlockState(blockEnd.getBlockPos()).isAir()){
                        BlockPos end = blockEnd.getBlockPos();

                        if(this.level().isClientSide){
                            for (int i = 0 ; i<3 ; i++){
                                this.level().addParticle(MGParticles.BEAM_EXPLOSION.get(),end.getX()+this.random.nextInt(-2,2),end.getY()+this.random.nextInt(0,2),end.getZ()+this.random.nextInt(-2,2),0.0F,0.0F,0.0F);
                            }
                        }else {
                            this.createExplosion(end);
                        }
                    }
                }
                if(!this.level().isClientSide && (this.getTarget() == null || this.getTarget().isAlive())){
                    LivingEntity livingEntity = this.level().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(30.0D),EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e->!this.is(e))).stream().findFirst().orElse(null);
                    if(livingEntity!=null){
                        this.setTarget(livingEntity);
                        this.laserPosition = livingEntity.position().add(0,livingEntity.getBbHeight()/2,0);
                        PacketHandler.sendToAllTracking(new PacketActionRay(this.getId(), (int) this.laserPosition.x, (int) this.laserPosition.y, (int) this.laserPosition.z),this);
                    }
                }
                if(this.laserPosition!=null){
                    this.setPos(this.position());

                    Vec3 vec32 = this.laserPosition.subtract(this.getHeadPos());
                    double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
                    double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F - this.getYRot();
                    this.rotHeadX = lerpRotation(this.rotHeadX,(float)f5 ,5.0F);

                    this.rotHeadY = Mth.clamp(lerpRotation(this.rotHeadY,(float) f6,5.0F),-45.0F,45.0F);
                }
            }
            if(this.laserTimer<=0){
                this.setLaser(false);
                this.cooldownLaser=50;
                this.laserPosition = null;
                if(!this.level().isClientSide){
                    this.level().broadcastEntityEvent(this,(byte) 17);
                }
            }
        }




        if(this.isAttacking()){
            this.attackTimer--;
            if(this.bladeOn()){
                if(this.attackTimer == 16){
                    if(this.level().isClientSide){
                        this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_SWORD_SWING.get(),SoundSource.NEUTRAL,2.0F,1.0F,false);
                    }
                    List<Entity> targets = this.level().getEntitiesOfClass(Entity.class,this.body.getBoundingBox().inflate(7,7,7), e -> e!=this.getControllingPassenger() && e != this && this.distanceTo(e) <= 20 + e.getBbWidth() / 2f && e.getY() <= this.getY() + 10);
                    for(Entity living : targets){
                        float entityHitAngle = (float) ((Math.atan2(living.getZ() - this.getZ(), living.getX() - this.getX()) * (180 / Math.PI) - 90) % 360);
                        float entityAttackingAngle = this.getYRot() % 360;
                        float arc = 180.0F;
                        if (entityHitAngle < 0) {
                            entityHitAngle += 360;
                        }

                        if (entityAttackingAngle < 0) {
                            entityAttackingAngle += 360;
                        }
                        float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
                        float entityHitDistance = (float) Math.sqrt((living.getZ() - this.getZ()) * (living.getZ() - this.getZ()) + (living.getX() - this.getX()) * (living.getX() - this.getX())) - living.getBbWidth() / 2f;
                        if(living instanceof  LivingEntity){
                            if (entityHitDistance <= 20 - 0.3 && (entityRelativeAngle <= arc / 2 && entityRelativeAngle >= -arc / 2) || (entityRelativeAngle >= 360 - arc / 2 || entityRelativeAngle <= -360 + arc / 2) ) {
                                this.doHurtTarget(living);
                                float f0=2.0F/living.getBbWidth() * 0.5F;
                                Vec3 vec3=new Vec3(living.getX()-this.getX(),0.0F,living.getZ()-this.getZ()).normalize().scale(f0);
                                living.push(vec3.x,vec3.y,vec3.z);
                            }
                        }
                    }
                }
            }else {
                if(this.attackTimer == 2){
                    if(this.level().isClientSide){
                        this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_STOMP.get(),SoundSource.NEUTRAL,2.0F,1.0F,false);
                        Map<BlockPos,Integer> posSet = new HashMap<>();
                        for (BlockPos pos2 : BlockPos.betweenClosed(this.getOnPos().offset(10,1,10),this.getOnPos().offset(-10,-1,-10))){
                            if(!this.level().getBlockState(pos2).isAir()){
                                float entityHitDistance = Math.max((float) Math.sqrt((pos2.getZ() - this.getZ()) * (pos2.getZ() - this.getZ()) + (pos2.getX() - this.getX()) * (pos2.getX() - this.getX())),0);
                                if (entityHitDistance <= 11 && entityHitDistance >=2) {
                                    Random random1 = new Random();
                                    double distance = 0.12F*Math.ceil(entityHitDistance) + random1.nextFloat(0.0F,1.0F);
                                    BlockPos.MutableBlockPos pos1 = pos2.mutable();
                                    boolean canSummon=true;
                                    for (int i=0;i<Mth.ceil(distance);i++){
                                        if(canSummon && !this.level().getBlockState(pos1.above()).isAir()){
                                            canSummon=false;
                                        }
                                    }
                                    if(canSummon){
                                        distance = Math.min(10,Math.ceil(entityHitDistance));
                                        posSet.put(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ()), (int) (distance));
                                    }
                                }
                            }
                        }
                        this.crackingBlock = posSet;
                        this.restoreCracking = 20;
                    }

                    this.pushEntities(this.level().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(10)));

                }
            }
            if(this.attackTimer==0){
                this.setIsAttacking(false);
            }
        }

        if(this.level().isClientSide){
            this.clientTick();
            if(this.restoreCracking>0){
                this.restoreCracking--;
                if(this.restoreCracking==0){
                    this.crackingBlock.clear();
                }
            }
        }

        this.rotHeadY0 = this.rotHeadY;
        this.rotHeadX0 = this.rotHeadX;

        while(this.rotHeadX - this.rotHeadX0 < -180.0F) {
            this.rotHeadX0 -= 360.0F;
        }

        while(this.rotHeadX - this.rotHeadX0 >= 180.0F) {
            this.rotHeadX0 += 360.0F;
        }
        while(this.rotHeadY - this.rotHeadY0 < -180.0F) {
            this.rotHeadY0 -= 360.0F;
        }

        while(this.rotHeadY - this.rotHeadY0 >= 180.0F) {
            this.rotHeadY0 += 360.0F;
        }
    }

    public BeamExplosionEntity createExplosion(BlockPos end){
        BeamExplosionEntity explosion = new BeamExplosionEntity(this.level(),this,null,new ExplosionDamageCalculator(),end.getX(),end.getY(),end.getZ(),4.0f,false, Explosion.BlockInteraction.DESTROY);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this.level(), explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(false);
        return explosion;
    }
    @Override
    public boolean doHurtTarget(Entity p_21372_) {
        boolean flag = super.doHurtTarget(p_21372_);
        if(flag){
            p_21372_.setSecondsOnFire(5);
        }
        return flag;
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {

    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isSwimming() {
        return super.isSwimming();
    }

    public Vec3 viewHeadY() {
        return this.calculateViewVector(this.rotHeadX,this.rotHeadY+this.getYRot());
    }

    private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
        float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);

        float clampedDelta = Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed);

        return currentYaw + clampedDelta;
    }

    public  List<EntityHitResult> getBeamEntityHitResult(Level p_150176_, Entity p_150177_, Vec3 p_150178_, Vec3 p_150179_, AABB p_150180_, Predicate<Entity> p_150181_, float p_150182_) {
        double d0 = Double.MAX_VALUE;
        List<EntityHitResult> results = new ArrayList<>();
        for(Entity entity1 : p_150176_.getEntities(p_150177_, p_150180_, p_150181_)) {
            AABB aabb = entity1.getBoundingBox().inflate((double)p_150182_);
            Optional<Vec3> optional = aabb.clip(p_150178_, p_150179_);
            if (optional.isPresent()) {
                results.add(new EntityHitResult(entity1));
            }
        }
        return results.isEmpty() ? null : results;
    }

    @Override
    public InteractionResult mobInteract(Player p_27584_, InteractionHand p_27585_) {
        this.doPlayerRide(p_27584_);
        return super.mobInteract(p_27584_, p_27585_);
    }

    @javax.annotation.Nullable
    public LivingEntity getControllingPassenger() {
        for(Entity entity:this.getPassengers()){
            return (LivingEntity) entity;
        }
        return null;
    }

    protected void doPlayerRide(Player pPlayer) {
        if (!this.level().isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }
    }

    @Override
    public boolean isSprinting() {
        return super.isSprinting();
    }

    public void travel(Vec3 pTravelVector) {
        LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
        if (this.isAlive()) {
            if (this.isVehicle() && livingentity!=null && !this.isImmobile()) {
                if(!this.isLaser()){
                    this.setYRot(livingentity.getYRot());
                    this.yRotO = this.getYRot();
                    this.setXRot(livingentity.getXRot() * 0.5F);
                    this.setRot(this.getYRot(), this.getXRot());
                    this.yBodyRot = this.getYRot();
                    this.yHeadRot = this.yBodyRot;
                    float f = livingentity.xxa * 0.5F;
                    float f1 = livingentity.zza;
                    if (f1 <= 0.0F) {
                        f1 *= 0.25F;
                    }

                    if (this.isControlledByLocalInstance()) {
                        float f0 =  this.isInWater() ? (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED)/2 :
                                this.isSprinting() ? (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) : (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED)/3 ;
                        this.setSpeed(f0);
                        if(this.isInWater()){
                            double d3 = livingentity.getLookAngle().y;
                            double d4 = d3 < -0.2D ? 0.085D : 0.06D;
                            if(f==0.0F || f1==0.0F){
                                d4-=d4;
                            }
                            this.moveRelative(this.getSpeed(),livingentity.getDeltaMovement().add(f,(d3 - livingentity.getDeltaMovement().y)* d4+(f+f1)*d3,f1));
                            super.travel(this.getDeltaMovement());

                        }else {
                            super.travel(new Vec3((double) f, pTravelVector.y, (double) f1));
                        }
                    } else if (livingentity instanceof Player) {
                        this.setDeltaMovement(Vec3.ZERO);
                    }
                }else {
                    this.rotHeadY = Mth.clamp(livingentity.getYRot() - this.getYRot(),-45.0F,45.0F);
                    //this.yRotO = this.getYRot();
                    this.rotHeadX = livingentity.getXRot() * 0.5F;
                    //this.setRot(this.getYRot(), this.getXRot());
                    //this.yBodyRot = this.getYRot();
                    //this.yHeadRot = this.yBodyRot;
                    if(this.rotHeadY<=-45.0F){
                        livingentity.setYRot(-45.0F+this.getYRot());
                    }
                    if(this.rotHeadY>=45.0F){
                        livingentity.setYRot(45.0F+this.getYRot());
                    }

                }
                this.calculateEntityAnimation( false);
                this.tryCheckInsideBlocks();
            } else {
                if(this.isControlledByLocalInstance() && this.isInWater()){
                    this.moveRelative(0.01F, pTravelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
                }else{
                    super.travel(pTravelVector);
                }
            }
        }
    }

    @Override
    public void positionRider(Entity pPassenger, MoveFunction moveFunction) {
        super.positionRider(pPassenger, moveFunction);
        if(this.getControllingPassenger()==pPassenger){
            if (pPassenger instanceof Mob mob) {
                this.yBodyRot = mob.yBodyRot;
            }
            pPassenger.setPos(this.getHeadPos());
        }
    }

    @Override
    public boolean isInWater() {
        return super.isInWater();
    }

    @Override
    public boolean is(Entity p_20356_) {
        for (int j=0 ; j<this.getParts().length ; j++){
            if(this.getParts()[j]==p_20356_){
                return true;
            }
        }
        for(Entity passanger : this.getPassengers()){
            if(passanger==p_20356_){
                return true;
            }
        }
        return super.is(p_20356_);
    }

    public boolean isLaser(){
        return this.entityData.get(LASER);
    }

    public void setLaser(boolean value){
        this.laserTimer = value ? 200 : 0;
        this.entityData.set(LASER,value);
    }

    private void checkTick() {
        this.isShooting=false;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(40.0D),EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e->!this.is(e)));
        for (TowerPart<?> part : this.towers){
            Optional<LivingEntity> optional = list.stream().findAny();
            if(optional.isPresent() && this.tickCount%5==0){
                if(!this.level().isClientSide){
                    LivingEntity target = optional.get();
                    BulletEntity arrow = new BulletEntity(this.level());
                    arrow.setOwner(this);
                    arrow.setPos(part.position());
                    arrow.shoot(target.getX()-part.getX(),target.getY()-part.getY(),target.getZ()-part.getZ(),1.5F,1.0F);
                    this.level().addFreshEntity(arrow);
                }
                this.isShooting=true;
            }
        }
    }
    private void towerTick() {
        this.isShooting=true;
        if(this.tickCount%5==0 && !this.level().isClientSide){
            BlockHitResult result = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.getControllingPassenger().getLookAngle().scale(30.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
            for (TowerPart<?> part : this.towers){
                BlockPos target = result.getBlockPos();
                BulletEntity arrow = new BulletEntity(this.level());
                arrow.setOwner(this);
                arrow.setPos(part.position());
                arrow.shoot(target.getX()-part.getX(),target.getY()-part.getY(),target.getZ()-part.getZ(),1.5F,1.0F);
                this.level().addFreshEntity(arrow);
            }
        }
    }

    @Override
    protected float nextStep() {
        return super.nextStep();
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
        this.playSound(CVNSounds.RAY_FOOTSTEP.get(),0.5F, 1.0F);
    }

    private void tickPart(TowerPart<?> p_31116_, double p_31117_, double p_31118_, double p_31119_) {
        p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
    }
    public void pushEntities(List<LivingEntity> list){
        for (LivingEntity livingEntity : list){
            if(!this.is(livingEntity) && livingEntity.hurt(this.damageSources().generic(),20.0F) ){
                double dx = livingEntity.getX() - this.getX();
                double dz = livingEntity.getZ() - this.getZ();
                double normalize = dx * dx + dz * dz;
                livingEntity.push(dx/normalize,1,dz/normalize);
            }
        }
    }
    protected void updateWalkAnimation(float p_268362_) {
        float f;
        if (this.getPose() == Pose.STANDING && !this.isInWater()) {
            f = Math.min(p_268362_ * 6.0F, 1.0F);
        } else {
            this.idleAnimationTimeout=1;
            this.idle.stop();
            f = 0.0F;
        }

        this.walkAnimation.update(f, 0.2F);
    }


    public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
        super.recreateFromPacket(p_218825_);
        if (true) return; // Forge: Fix MC-158205: Moved into setId()
        TowerPart<?>[] part = this.parts;


        for(int i = 0; i < part.length; ++i) {
            part[i].setId(i + p_218825_.getId());
        }

    }

    @Override
    public float getStepHeight() {
        return 3.0F;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return this.parts;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    public void clientTick(){
        if(this.isInWater()){
            if (this.swimAnimationTimeout <= 0) {
                this.swimAnimationTimeout = 35;
                this.stomp.stop();
                this.meleeAttack.stop();
                this.idle.stop();
                this.in_water.start(this.tickCount);
                this.idleAnimationTimeout=0;
            } else {
                --this.swimAnimationTimeout;
            }
        }else {
            if (this.idleAnimationTimeout <= 0) {
                this.idleAnimationTimeout = 80;
                this.in_water.stop();
                this.stomp.stop();
                this.meleeAttack.stop();
                this.idle.start(this.tickCount);
                this.swimAnimationTimeout=0;
            } else {
                --this.idleAnimationTimeout;
            }
        }

        if(this.smokeEffectTimer>0){
            this.smokeEffectTimer--;
            Vec3 head = this.getHeadPos();
            for(int j = 0 ; j<3 ; j++){
                this.level().addParticle(ParticleTypes.SMOKE,head.x,head.y,head.z,0.0F,0.0F,0.0F);
            }
        }

        if(this.prepare_laser.isStarted()){
            if(this.idleAnimationTimeout==13){
                this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_CHARGE_LASER.get(),SoundSource.NEUTRAL,2.0F,1.0F,false);
            }
        }
        if(Minecraft.getInstance().player!=null && Minecraft.getInstance().player.getVehicle()==this){
            if(Minecraft.getInstance().options.keySprint.isDown()){
                PacketHandler.sendToServer(new PacketKeySync(2));
            }else {
                PacketHandler.sendToServer(new PacketKeySync(3));
            }
        }
    }
    private void setIsAttacking(boolean b) {
        this.entityData.set(ATTACKING,b);
    }

    private boolean isAttacking(){
        return this.entityData.get(ATTACKING);
    }
    public boolean towerOn(){
        return this.entityData.get(TOWER_ON);
    }
    private void setTowerOn(boolean b) {
        this.entityData.set(TOWER_ON,b);
    }
    private void setBladeOn(boolean b) {
        this.entityData.set(BLADE_ON,b);
    }
    public boolean bladeOn(){
        return this.entityData.get(BLADE_ON);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING,false);
        this.entityData.define(BLADE_ON,false);
        this.entityData.define(TOWER_ON,false);
        this.entityData.define(LASER,false);
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.setIsAttacking(true);
            this.idle.stop();
            this.attackTimer = 40;
            this.idleAnimationTimeout=40;
            this.stomp.start(this.tickCount);
        }if(p_21375_==5){
            this.setIsAttacking(true);
            this.attackTimer = 35;
            this.idle.stop();
            this.idleAnimationTimeout=24;
            this.meleeAttack.start(this.tickCount);
        }else if(p_21375_==8){
            this.setTowerOn(false);
            this.tower_off.start(this.tickCount);
        }else if(p_21375_==12){
            this.setTowerOn(true);
            this.tower_on.start(this.tickCount);
        }else if(p_21375_==9){
            this.setBladeOn(false);
            this.blade_off.start(this.tickCount);
        }else if(p_21375_==11){
            this.setBladeOn(true);
            this.blade_on.start(this.tickCount);
        }else if(p_21375_==13){
            this.prepare_laser.start(this.tickCount);
            this.prepareLaserTimer = 58;
        }else if(p_21375_==14){
            this.setLaser(true);
        }else if(p_21375_==15){
            if(this.towerOn()){
                this.tower_on.stop();
                this.tower_off.start(this.tickCount);
            }else {
                this.tower_off.stop();
                this.tower_on.start(this.tickCount);
            }
            this.setTowerOn(!this.towerOn());
        }else if(p_21375_==16){
            if(this.bladeOn()){
                this.blade_on.stop();
                this.blade_off.start(this.tickCount);
            }else {
                this.blade_off.stop();
                this.blade_on.start(this.tickCount);
            }
            this.setBladeOn(!this.bladeOn());
        }else if(p_21375_==17){
            this.cooldownLaser=50;
            this.smokeEffectTimer=100;
            this.setLaser(false);
            this.idleAnimationTimeout=0;
            this.laser.stop();
            this.laserPosition = null;
        }
        super.handleEntityEvent(p_21375_);
    }

    @Override
    public void onPlayerJump(int p_30591_) {
        if (p_30591_ < 0) {
            p_30591_ = 0;
        }


        if (p_30591_ >= 90) {
            this.jumpPendingScale = 1.0F;
        } else {
            this.jumpPendingScale = 0.4F + 0.4F * (float)p_30591_ / 90.0F;
        }
    }

    @Override
    public boolean canJump() {
        return !this.isInWater();
    }

    @Override
    public void handleStartJump(int p_21695_) {
        this.playSound(CVNSounds.RAY_JUMP.get(), 1.0F, 1.0F);
    }

    @Override
    public void handleStopJump() {

    }

    @Override
    public double getRiderShieldingHeight() {
        return 5;
    }

    @Override
    public void handleKey(int key) {
        if(key==0){
            if(!this.isInWater() && !this.isAttacking()){
                MetalGearRayEntity.this.setIsAttacking(true);
                if(this.bladeOn()){
                    this.attackTimer=35;
                    if(!MetalGearRayEntity.this.level().isClientSide){
                        MetalGearRayEntity.this.level().broadcastEntityEvent(MetalGearRayEntity.this,(byte) 5);
                    }
                }else {
                    this.attackTimer=40;
                    if(!MetalGearRayEntity.this.level().isClientSide){
                        MetalGearRayEntity.this.level().broadcastEntityEvent(MetalGearRayEntity.this,(byte) 4);
                    }
                }
            }
        }else if(MGKeybinds.attackKey4.getKey().getValue()==key){
            if(!this.isInWater()){
                if(!this.level().isClientSide){
                    if(this.prepareLaserTimer<=0){
                        if(this.isLaser()){
                            this.level().playSound(null,this, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,2.0F,1.0F);
                            this.setLaser(false);
                            this.cooldownLaser=50;
                            this.level().broadcastEntityEvent(this,(byte) 17);
                        }else {
                            this.level().broadcastEntityEvent(this,(byte) 13);
                            this.prepareLaserTimer = 58;
                        }
                    }
                }
            }
        }else if(MGKeybinds.attackKey3.getKey().getValue()==key){
            if(this.bladeOn()){
                this.level().playSound(null,this, SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL,2.0F,6.0F);
            }else {
                this.level().playSound(null,this, SoundEvents.PISTON_EXTEND, SoundSource.NEUTRAL,2.0F,6.0F);
            }
            this.setBladeOn(!this.bladeOn());
            this.level().broadcastEntityEvent(this,(byte) 16);

        }else {
            if(this.towerOn()){
                this.level().playSound(null,this, SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL,2.0F,1.0F);
            }else {
                this.level().playSound(null,this, SoundEvents.PISTON_EXTEND, SoundSource.NEUTRAL,2.0F,1.0F);
            }
            this.setTowerOn(!this.towerOn());
            this.level().broadcastEntityEvent(this,(byte) 15);
        }
    }

    class RayAttackGoal extends MeleeAttackGoal {
        public StratAttack currentAttack = StratAttack.STOMP;
        public int nextTimerStrat = 0;
        public int maxTimerStrat = 0;
        public RayAttackGoal(PathfinderMob p_25552_, double p_25553_, boolean p_25554_) {
            super(p_25552_, p_25553_, p_25554_);
        }

        @Override
        public void start() {
            super.start();
            this.currentAttack = StratAttack.STOMP;
            this.maxTimerStrat= 200 + MetalGearRayEntity.this.level().random.nextInt(10)*MetalGearRayEntity.this.level().random.nextInt(5);
        }

        @Override
        public boolean canUse() {
            return !MetalGearRayEntity.this.isVehicle() && super.canUse() && !MetalGearRayEntity.this.isLaser();
        }

        @Override
        public void tick() {
            StratAttack strat = this.currentAttack;
            if(strat == StratAttack.STOMP){
                super.tick();
            }else {
                MetalGearRayEntity.this.getNavigation().stop();
            }
            if(!MetalGearRayEntity.this.isAttacking() && this.nextTimerStrat++>this.maxTimerStrat){
                this.nextTimerStrat = 0;
                this.maxTimerStrat= 20 + MetalGearRayEntity.this.level().random.nextInt(3)*MetalGearRayEntity.this.level().random.nextInt(3);
                int nextStrat = MetalGearRayEntity.this.cooldownLaser<=0 ? MetalGearRayEntity.this.random.nextInt(0,1) : 1;
                this.switchStrat(nextStrat);
            }
        }

        public void switchStrat(int idStrat){
            switch (idStrat){
                case 0 ->{
                    this.currentAttack = StratAttack.LASER;
                    MetalGearRayEntity.this.prepareLaserTimer = 58;
                    MetalGearRayEntity.this.level().broadcastEntityEvent(MetalGearRayEntity.this,(byte) 13);
                }
                case 1 ->{
                    this.currentAttack = StratAttack.STOMP;
                }
            }
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
            double d0 = this.getAttackReachSqr(p_25557_) + 10.0D;
            if (p_25558_ <= d0 && this.getTicksUntilNextAttack()<=0 && MetalGearRayEntity.this.attackTimer<=0) {
                this.resetAttackCooldown();
            }
        }


        @Override
        protected void resetAttackCooldown() {
            super.resetAttackCooldown();
            MetalGearRayEntity.this.setBladeOn(false);
            MetalGearRayEntity.this.setIsAttacking(true);
            MetalGearRayEntity.this.attackTimer = 40;
            if(!MetalGearRayEntity.this.level().isClientSide){
                MetalGearRayEntity.this.level().broadcastEntityEvent(MetalGearRayEntity.this,(byte) 4);
            }
        }
        enum StratAttack{
            LASER,
            STOMP;
        }
    }


}
