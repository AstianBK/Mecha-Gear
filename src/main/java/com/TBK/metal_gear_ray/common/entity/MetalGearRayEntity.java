package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.client.animations.MetalGearRayAnim;
import com.TBK.metal_gear_ray.common.api.IMecha;
import com.TBK.metal_gear_ray.common.network.PacketHandler;
import com.TBK.metal_gear_ray.common.network.messager.PacketActionRay;
import com.TBK.metal_gear_ray.common.network.messager.PacketKeySync;
import com.TBK.metal_gear_ray.common.network.messager.PacketMissileTarget;
import com.TBK.metal_gear_ray.common.register.CVNItems;
import com.TBK.metal_gear_ray.common.register.CVNSounds;
import com.TBK.metal_gear_ray.common.register.MGParticles;
import com.TBK.metal_gear_ray.server.capability.ArsenalCapability;
import com.TBK.metal_gear_ray.server.keybind.MGKeybinds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Predicate;

public class MetalGearRayEntity extends PathfinderMob implements ContainerListener, HasCustomInventoryScreen,PlayerRideableJumping, RiderShieldingMount, IMecha,OwnableEntity {
    public static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> LASER =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> AMOUNT =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Boolean> TOWER_ON =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> BLADE_ON =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(MetalGearRayEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public AnimationState stomp = new AnimationState();
    public AnimationState meleeAttack = new AnimationState();
    public AnimationState idle = new AnimationState();
    public AnimationState inWater = new AnimationState();
    public AnimationState isAir = new AnimationState();
    public AnimationState bladeOn = new AnimationState();
    public AnimationState bladeOff = new AnimationState();
    public AnimationState towerOn = new AnimationState();
    public AnimationState towerOff = new AnimationState();
    public AnimationState prepareLaser = new AnimationState();
    public AnimationState laser = new AnimationState();
    private int idleAnimationTimeout;
    private int swimAnimationTimeout;
    private int attackTimer;
    private final TowerPart<?> missileSlot0;
    private final TowerPart<?> missileSlot1;
    private final TowerPart<?> missileSlot2;

    private final TowerPart<?> tower0;
    private final TowerPart<?> tower1;
    private final TowerPart<?> tower2;
    private final TowerPart<?> tower3;
    private final TowerPart<?> head;
    private final TowerPart<?> body;
    private final TowerPart<?>[] slots;
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
    public int cooldownJump = 0;
    public Map<BlockPos,Integer> crackingBlock = new HashMap<>();
    public int restoreCracking = 0;
    public int cooldownAmount = 0;
    public int smokeEffectTimer = 0;
    public int interpolationCamTimer = 0;
    public int interpolationCamTimer0 = 0;
    public int tickReload=0;
    protected SimpleContainer inventory;
    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;
    private Vec3 positionOld = Vec3.ZERO;
    protected static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(MetalGearRayEntity.class,
            EntityDataSerializers.BOOLEAN);
    private boolean step1Played = false;
    private boolean step2Played = false;
    private boolean lift1Played = false;
    private boolean lift2Played = false;

    private boolean passengersWaterBuffer = false;

    public MetalGearRayEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);

        this.missileSlot0 = new TowerPart<>(this,"missileSlot0",1,1);
        this.missileSlot1 = new TowerPart<>(this,"missileSlot1",1,1);
        this.missileSlot2 = new TowerPart<>(this,"missileSlot2",1,1);

        this.tower0 = new TowerPart<>(this,"tower0",1,1);
        this.tower1 = new TowerPart<>(this,"tower1",1,1);
        this.tower2 = new TowerPart<>(this,"tower2",1,1);
        this.tower3 = new TowerPart<>(this,"tower3",1,1);
        this.head = new TowerPart<>(this,"head",3,3);
        this.body = new TowerPart<>(this,"body",10,10);

        this.slots = new TowerPart[]{this.missileSlot0,this.missileSlot1,this.missileSlot2};
        this.towers = new TowerPart[]{this.tower0,this.tower1,this.tower2,this.tower3};
        this.bodyParts = new TowerPart[]{this.body,this.head};
        this.parts = new TowerPart[]{this.missileSlot0,this.missileSlot1,this.missileSlot2,this.tower0,this.tower1,this.tower2,this.tower3,this.body,this.head};
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
    }

    @Override
    public void setId(int p_20235_) {
        super.setId(p_20235_);
        for (int i = 0; i < this.parts.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.parts[i].setId(p_20235_ + i + 1);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
        this.createInventory();
        this.setAmount(12);
        return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_, p_21438_);
    }

    public static AttributeSupplier setAttributes() {
        return MetalGearRayEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR,40.0D)
                .add(Attributes.ARMOR_TOUGHNESS,10.0D)
                .add(Attributes.FOLLOW_RANGE, 45.D)
                .add(Attributes.MOVEMENT_SPEED, 0.15d)
                .add(Attributes.ATTACK_DAMAGE,12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F)
                .add(Attributes.JUMP_STRENGTH,0.43F)
                .build();

    }

    protected int getInventorySize() {
        return 54;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3,new RayAttackGoal(this,2.0D,false));
        this.targetSelector.addGoal(4, new OwnerHurtTargetMetalGearGoal(this));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this));
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
            LivingEntity passanger = this.getControllingPassenger();
            Vec3 view = this.calculateViewVector(passanger.getViewXRot(1.0F)- 7.0F,passanger.getYHeadRot());
            direction = this.getHeadPos().add(view.scale(50.0D)).subtract(this.getHeadPos().add(view.scale(1D)));
        }else {
            direction = this.getHeadPos().add(this.viewHeadY().scale(50.0D)).subtract(this.getHeadPos().add(this.viewHeadY().scale(0.5D)));
        }
        return direction;
    }


    protected void executeRidersJump(float p_251967_, Vec3 p_275627_) {
        double d0 = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.getBlockJumpFactor() + (double)this.getJumpBoostPower();
        this.addDeltaMovement(this.getLookAngle().multiply(4.0D, 0.0D, 4.0D).normalize().scale((double)(22.2222F * p_251967_) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0D, (double)(this.isInWater() ? 10.0F  : 4.4285F  * p_251967_) * d0, 0.0D));
        this.setIsJumping(true);
        this.hasImpulse = true;
    }

    protected void tickRidden(Player p_278233_, Vec3 p_275693_) {
        super.tickRidden(p_278233_, p_275693_);


        if(this.isControlledByLocalInstance()){
            if (this.onGround() || this.isInWater()) {
                if(this.isJumping()){
                    if(this.level().isClientSide){
                        this.applyRadius(10,0.2F,this.position());
                        PacketHandler.sendToServer(new PacketKeySync(4));

                        this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_STOMP.get(),SoundSource.NEUTRAL,2.0F,1.0F,false);
                        Map<BlockPos,Integer> posSet = new HashMap<>();
                        for (BlockPos pos2 : BlockPos.betweenClosed(this.getOnPos().offset(10,1,10),this.getOnPos().offset(-10,-1,-10))){
                            if(!this.level().getBlockState(pos2).isAir()){
                                float entityHitDistance = Math.max((float) Math.sqrt((pos2.getZ() - this.getZ()) * (pos2.getZ() - this.getZ()) + (pos2.getX() - this.getX()) * (pos2.getX() - this.getX())),0);
                                if (entityHitDistance <= 12 && entityHitDistance >=2) {
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
                                        distance = Mth.clamp((12.0F/entityHitDistance),0.0F,1.0f);
                                        posSet.put(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ()), (int) (distance));
                                        for(int j=0;j<this.level().random.nextInt(5,10);j++){
                                            Minecraft.getInstance().particleEngine.crack(pos2, Direction.UP);
                                        }
                                    }
                                }
                            }
                        }
                        this.crackingBlock = posSet;
                        this.restoreCracking = 200;
                    }
                }
                this.setIsJumping(false);
                if (this.jumpPendingScale>0.0F && !this.isJumping()) {
                    this.executeRidersJump(1.0F, p_275693_);
                }

                this.jumpPendingScale = 0.0F;
            }
        }else {
            if(p_278233_.getEffect(MobEffects.NIGHT_VISION)==null){
                if(p_278233_.isInWater()){
                    p_278233_.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,9999999,2),this);
                    this.passengersWaterBuffer = true;
                }
            }else {
                if(!p_278233_.isInWater() && this.passengersWaterBuffer){
                    p_278233_.removeEffect(MobEffects.NIGHT_VISION);
                    this.passengersWaterBuffer=false;
                }
            }
            if(p_278233_.isInWater()){
                p_278233_.setAirSupply(300);
            }
        }
        p_278233_.clearFire();

    }

    public void setSitting(boolean isSitting){
        this.entityData.set(SITTING,isSitting);
    }
    public boolean isSitting(){
        return this.entityData.get(SITTING);
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
        p_21017_ = Math.min(50,p_21017_);

        if(p_21016_.getEntity() instanceof Player ){
            this.lastHurtByPlayerTime = this.tickCount;
        }
        return super.hurt(p_21016_, p_21017_);
    }

    @javax.annotation.Nullable
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    private static float getElapsedSeconds(AnimationDefinition p_232317_, long p_232318_) {
        float f = (float)p_232318_ / 1000.0F;
        return p_232317_.looping() ? f % p_232317_.lengthInSeconds() : f;
    }

    @Override
    public void die(DamageSource p_21014_) {
        if(this.level().isClientSide){
            for (int i = 0 ; i<30 ; i++){
                this.level().addParticle(MGParticles.BEAM_EXPLOSION.get(),this.getX()+this.random.nextInt(-30,30),this.getY()+this.random.nextInt(0,10),this.getZ()+this.random.nextInt(-30,30),0.0F,0.0F,0.0F);
            }
        }
        this.level().explode(this,this.getX(),this.getY(),this.getZ(),30.0F, Level.ExplosionInteraction.NONE);
        if(this.getOwner() instanceof Player player){
            ArsenalCapability cap = ArsenalCapability.get(player);
            if(cap!=null){
                cap.rayDie();
            }
        }
        super.die(p_21014_);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.getOwner() instanceof Player player){
            ArsenalCapability cap = ArsenalCapability.get(player);
            if(cap!=null){
                cap.lastPos = new BlockPos(this.blockPosition().getX(),this.blockPosition().getY(),this.blockPosition().getZ());
                cap.lastDimension = this.level().dimension();
            }
        }

        if(this.lastHurtByPlayerTime + 250 < this.tickCount || this.getLastHurtMobTimestamp()+250<this.tickCount){
            if(this.getMaxHealth()!=this.getHealth()){
                if(this.tickCount%100==0){
                    if(!this.level().isClientSide){
                        this.heal(1);
                    }
                    this.spawParticleHeal();
                }
            }
        }

        if(this.getAmount()<12){
            cooldownAmount--;
            if(cooldownAmount<=0){
                if(!this.level().isClientSide && this.getControllingPassenger()!=null){
                    this.level().playSound(null,this.getControllingPassenger(),CVNSounds.RAY_MISSILE_RELOAD.get(),SoundSource.NEUTRAL,5.0F,1.0F);
                }
                this.plusAmount(3);
                cooldownAmount=200;
            }
        }
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

        if(this.cooldownJump>0){
            this.cooldownJump--;
        }

        this.tickParts();

        this.tickLaser();

        this.tickAttack();

        this.rotHeadY0 = this.rotHeadY;
        this.rotHeadX0 = this.rotHeadX;

        this.interpolationCamTimer0 = this.interpolationCamTimer;

        if(this.level().isClientSide){
            this.clientTick();
        }else{
            this.serverTick();
        }
        if (!this.level().isClientSide && this.tickCount%20==0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            boolean flag = false;
            AABB aabb = this.body.getBoundingBox().inflate(1D);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
                BlockState blockstate = this.level().getBlockState(blockpos);
                Block block = blockstate.getBlock();
                if (block instanceof LeavesBlock || blockstate.is(BlockTags.LOGS)) {
                    flag = this.level().destroyBlock(blockpos, true, this) || flag;
                }
            }
        }
        if(this.tickReload>0){
            this.tickReload--;
        }

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

    private void tickLaser() {
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
                        PacketHandler.sendToAllTracking(new PacketActionRay(this.getId(), (int) this.getTarget().getX(), (int) this.getTarget().getY(), (int) this.getTarget().getZ(),0),this);
                    }
                }else {
                    this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_SHOOT_LASER.get(),SoundSource.NEUTRAL,3.0F,1.0F+3.0F*this.random.nextFloat(),false);
                    this.idleAnimationTimeout = 200;
                    this.prepareLaser.stop();
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
                if(this.tickCount%2 == 0 && this.getControllingPassenger()!=null){
                    BlockHitResult blockEnd = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.calculateViewVector(this.getControllingPassenger().getViewXRot(1.0F)- 7.0F,this.getControllingPassenger().getYHeadRot()).scale(50.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
                    List<EntityHitResult> hits = this.getBeamEntityHitResult(this.level(),this,this.getHeadPos(),this.getHeadPos().add(this.calculateViewVector(this.getControllingPassenger().getViewXRot(1.0F)- 7.0F,this.getControllingPassenger().getYHeadRot()).scale(50.0D)),this.getBoundingBox().inflate(100.0F), e->!this.is(e),0.5F);
                    if(hits!=null){
                        for (EntityHitResult hit : hits){
                            if(hit.getEntity() instanceof LivingEntity){
                                LivingEntity entity = (LivingEntity) hit.getEntity();
                                if(entity.hurt(this.damageSources().generic(),4.0F)){
                                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,3,10));
                                }
                            }
                        }
                    }
                    if(!level().getBlockState(blockEnd.getBlockPos()).isAir()){
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
                if(this.tickCount%2 == 0){
                    BlockHitResult blockEnd = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.viewHeadY().scale(50.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
                    List<EntityHitResult> hits = this.getBeamEntityHitResult(this.level(),this,this.getHeadPos(),this.getHeadPos().add(this.viewHeadY().scale(50.0D)),this.getBoundingBox().inflate(100.0F), e->!this.is(e),0.5F);
                    if(hits!=null){
                        for (EntityHitResult hit : hits){
                            if(hit.getEntity() instanceof LivingEntity){
                                LivingEntity entity = (LivingEntity) hit.getEntity();
                                if(entity.hurt(this.damageSources().generic(),4.0F)){
                                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,3,10));

                                }
                            }
                        }
                    }
                    if(!level().getBlockState(blockEnd.getBlockPos()).isAir()){
                        BlockPos end = blockEnd.getBlockPos();

                        if(this.level().isClientSide){
                            for (int i = 0 ; i<3 ; i++){
                                this.level().addParticle(MGParticles.BEAM_EXPLOSION.get(),end.getX()+this.random.nextInt(-2,2),end.getY()+this.random.nextInt(0,2),end.getZ()+this.random.nextInt(-2,2),0.0F,0.0F,0.0F);
                            }
                        }else {
                            this.createExplosion(end);
                        }
                    }

                    if(!this.level().isClientSide){
                        if(!this.isValidTarget(this.getTarget())){
                            this.setTarget(null);
                            LivingEntity livingEntity = this.level().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(40.0D),EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e->!this.is(e) && this.isValidTarget(e))).stream().findFirst().orElse(null);
                            if(livingEntity!=null){
                                this.setTarget(livingEntity);
                                this.laserPosition = livingEntity.position().add(0,livingEntity.getBbHeight(),0);
                                PacketHandler.sendToAllTracking(new PacketActionRay(this.getId(), (int) this.laserPosition.x, (int) this.laserPosition.y, (int) this.laserPosition.z,0),this);
                            }
                            if(this.getTarget()==null){
                                this.setLaser(false);
                                this.cooldownLaser=600;
                                this.laserPosition = null;
                                if(!this.level().isClientSide){
                                    this.level().broadcastEntityEvent(this,(byte) 17);
                                }
                            }
                        }
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
                this.cooldownLaser=600;
                this.laserPosition = null;
                if(!this.level().isClientSide){
                    this.level().broadcastEntityEvent(this,(byte) 17);
                }
            }
        }
    }

    private void tickAttack() {
        if(this.isAttacking()){
            this.attackTimer--;
            if(this.bladeOn()){
                if(this.attackTimer == 16){
                    if(this.level().isClientSide){
                        this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_SWORD_SWING.get(),SoundSource.NEUTRAL,4.0F,1.0F,false);
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
                                living.hurt(damageSources().mobAttack(this),30.0F);
                                living.setSecondsOnFire(3);
                                float f0=2.0F/living.getBbWidth() * 0.5F;
                                Vec3 vec3=new Vec3(living.getX()-this.getX(),0.0F,living.getZ()-this.getZ()).normalize().scale(f0);
                                living.push(vec3.x,vec3.y,vec3.z);
                            }
                        }
                    }
                }
            }else {
                if(this.attackTimer == 5){
                    if(this.level().isClientSide){
                        this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_STOMP.get(),SoundSource.NEUTRAL,2.0F,1.0F,false);
                        Map<BlockPos,Integer> posSet = new HashMap<>();
                        for (BlockPos pos2 : BlockPos.betweenClosed(this.getOnPos().offset(10,1,10),this.getOnPos().offset(-10,-1,-10))){
                            if(!this.level().getBlockState(pos2).isAir()){
                                float entityHitDistance = Math.max((float) Math.sqrt((pos2.getZ() - this.getZ()) * (pos2.getZ() - this.getZ()) + (pos2.getX() - this.getX()) * (pos2.getX() - this.getX())),0);
                                if (entityHitDistance <= 12 && entityHitDistance >=2) {
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
                                        distance = Mth.clamp((12.0F/entityHitDistance),0.0F,1.0f);
                                        posSet.put(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ()), (int) (distance));
                                        for(int j=0;j<this.level().random.nextInt(5,10);j++){
                                            Minecraft.getInstance().particleEngine.crack(pos2, Direction.UP);
                                        }
                                    }
                                }
                            }
                        }
                        this.crackingBlock = posSet;
                        this.restoreCracking = 200;
                    }

                    this.upEntities(this.level().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(10)));

                }
            }
            if(this.attackTimer==0){
                this.setIsAttacking(false);
            }
        }
    }

    public void tickStep(){
        float animationProgress = this.walkAnimation.position();

        long u = (long)(animationProgress * 50.0F * 2.0F);

        float seconds = getElapsedSeconds(MetalGearRayAnim.walklegs,u);

        if (!step1Played && seconds >= 1.755F) {
            level().playLocalSound(getX(), getY(), getZ(),
                    CVNSounds.RAY_FOOTSTEP2.get(), SoundSource.HOSTILE, 10.0F, 1.0F,false);
            step1Played = true;
        }

        if (!step2Played && seconds >= 2.869F) {
            level().playLocalSound(getX(), getY(), getZ(),
                    CVNSounds.RAY_FOOTSTEP2.get(), SoundSource.HOSTILE, 10.0F, 1.0F,false);
            step2Played = true;
        }

        if (!lift1Played && seconds >= 0.271F) {
            level().playLocalSound(getX(), getY(), getZ(),
                    CVNSounds.RAY_FOOTSTEP1.get(), SoundSource.HOSTILE, 10.0F, 1.0F,false);
            lift1Played = true;
        }

        if (!lift2Played && seconds >= 2.088F) {
            level().playLocalSound(getX(), getY(), getZ(),
                    CVNSounds.RAY_FOOTSTEP1.get(), SoundSource.HOSTILE, 10.0F, 1.0F,false);
            lift2Played = true;
        }

        // Reiniciar pasos si termina el ciclo de animación
        if (seconds<0.200F) {
            step1Played = false;
            step2Played = false;
            lift1Played = false;
            lift2Played = false;
        }
    }
    private void tickParts() {
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
        Vec3[] posSlot = new Vec3[this.slots.length];
        for (int j = 0 ; j<this.slots.length ; j++){
            posSlot[j]=new Vec3(this.slots[j].getX(), this.slots[j].getY(), this.slots[j].getZ());
        }
        for (int k=0 ; k<slots.length ; k++){
            double leg0X =  ( (5-k*2) * cos) + ( (-2.5F) * sin);
            double leg0Z =  ( (5-k*2) * sin) - ( (-2.5F) * cos);

            this.tickPart(slots[k],leg0X,8,leg0Z);

            this.slots[k].xo = posSlot[k].x;
            this.slots[k].yo = posSlot[k].y;
            this.slots[k].zo = posSlot[k].z;
            this.slots[k].xOld = posSlot[k].x;
            this.slots[k].yOld = posSlot[k].y;
            this.slots[k].zOld = posSlot[k].z;
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


            if(!this.isVehicle()){
                this.checkTick();
            }else {
                this.towerTick();
            }
        }
    }

    private boolean isValidTarget(Entity target) {
        if(target==null || !target.isAlive()){
            return false;
        }
        Vec3 vec32 = target.position().subtract(this.getHeadPos());
        double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
        double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F - this.getYRot();

        return (f6<=45.0F ||  f6>=-45.0F);
    }

    public BeamExplosionEntity createExplosion(BlockPos end){
        BeamExplosionEntity explosion = new BeamExplosionEntity(this.level(),this,null,new ExplosionDamageCalculator(),end.getX(),end.getY(),end.getZ(),4.0f,false, Explosion.BlockInteraction.DESTROY);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this.level(), explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(false);
        return explosion;
    }

    public void applyRadius(float radius, float speedY, Vec3 pos){
        int i;
        i = Mth.ceil((float)Math.PI * radius * radius);
        float radius1 = (float)Math.PI /(float) i;
        Random random1 = new Random();
        for(int j=0;j<=i*2;j++){
            float f1 = Mth.sin(j * radius1)*radius;
            float f2 = Mth.cos(j * radius1)*radius;
            this.level().addParticle(ParticleTypes.POOF,(float)pos.x+f2 ,(float)pos.y,(float)pos.z+f1,
                    0.01F,
                    random1.nextFloat(0.0F,speedY),
                    0.01F);
        }
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {

    }


    @Override
    public boolean fireImmune() {
        return true;
    }

    public void knockBack(List<Entity> p_31132_, boolean up) {
        double d0 = (this.getBoundingBox().minX + this.getBoundingBox().maxX) / 2.0D;
        double d1 = (this.getBoundingBox().minZ + this.getBoundingBox().maxZ) / 2.0D;

        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                this.knockBack((LivingEntity) entity,d0,d1);
            }
        }
    }
    private void knockBack(LivingEntity entity,double d0,double d1) {
        double d2 = entity.getX() - d0;
        double d3 = entity.getZ() - d1;
        double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
        entity.push(d2 / d4 * 16.0D,(double)0.2F, d3 / d4 * 16.0D);
        entity.hurt(damageSources().mobAttack(this), 2F);
        this.doEnchantDamageEffects(this, entity);

    }
    public float getCamInterpolation(float partialTicks){
        float time = Mth.lerp(partialTicks,interpolationCamTimer0,interpolationCamTimer);
        return 1.0F-(time/10.0F);
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
        ItemStack stack = p_27584_.getItemInHand(p_27585_);
        if(stack.is(Items.NETHERITE_INGOT) && this.getMaxHealth()!=this.getHealth()){
            if(!this.level().isClientSide){
                this.heal(30.0F);
            }
            this.spawParticleHeal();

            if(!p_27584_.getAbilities().instabuild){
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        if(stack.isEmpty() && p_27584_.isShiftKeyDown()){
            this.setSitting(!this.isSitting());
            if(p_27584_.level().isClientSide){
                p_27584_.displayClientMessage(Component.translatable("ray."+(this.isSitting() ? "sitting" : "follow")),true);
            }
            return InteractionResult.SUCCESS;

        }
        if(this.getOwnerUUID()!=null){
            if(stack.is(CVNItems.DEPLOYER.get()) && this.getOwnerUUID().equals(p_27584_.getUUID())){
                ArsenalCapability arsenal = ArsenalCapability.get(p_27584_);
                if(arsenal!=null && arsenal.rayActive){
                    if(p_27584_.level().isClientSide){
                        p_27584_.level().playLocalSound(p_27584_.getX(),p_27584_.getY(),p_27584_.getZ(),SoundEvents.BEACON_DEACTIVATE,SoundSource.HOSTILE,5.0F,1.0F,false);
                        p_27584_.displayClientMessage(Component.translatable("ray.save"),true);
                    }else {
                        arsenal.saveRay(this,p_27584_);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            if(this.getOwnerUUID().equals(p_27584_.getUUID())){
                this.doPlayerRide(p_27584_);
            }
        }
        return super.mobInteract(p_27584_, p_27585_);
    }

    public void spawParticleHeal() {
        Random random = new Random();
        for (int i = 0 ; i<5 ; i++){
            double box = this.getBbWidth();
            double xp = this.getX() + random.nextDouble(-box, box);
            double yp = this.getY() + random.nextDouble(0.0d, this.getBbHeight());
            double zp = this.getZ() + random.nextDouble(-box, box);
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,xp,yp,zp,0.0F,0.0F,0.0F);
        }
    }
    @javax.annotation.Nullable
    public LivingEntity getControllingPassenger() {
        for(Entity entity:this.getPassengers()){
            return (LivingEntity) entity;
        }
        return null;
    }

    protected void doPlayerRide(Player pPlayer) {
        this.setSitting(false);
        this.setTowerOn(false);
        if (!this.level().isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance p_21197_) {
        return false;
    }

    @Override
    public void checkDespawn() {

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
        }else{
            super.travel(pTravelVector);
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
    public int getJumpCooldown() {
        return this.cooldownJump;
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
        return super.is(p_20356_) || (this.getOwner()!=null && this.getOwner().is(p_20356_));
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
        for (TowerPart<?> part : this.towers){
            LivingEntity target = this.getTarget();
            if(target!=null && this.tickCount%5==0){
                if(!this.level().isClientSide){
                    BulletEntity arrow = new BulletEntity(this.level());
                    arrow.setOwner(this);
                    arrow.setPos(part.position());
                    arrow.shoot(target.getX()-part.getX(),target.getY()-part.getY(),target.getZ()-part.getZ(),1.5F,1.0F);
                    this.level().addFreshEntity(arrow);
                    this.level().playSound(null,this,CVNSounds.RAY_TURRET_SHOOT3.get(),SoundSource.NEUTRAL,1.0F,1);
                }
                this.isShooting=true;
            }
        }
    }

    private void towerTick() {
        this.isShooting=true;
        if(this.tickCount%5==0 && !this.level().isClientSide){
            LivingEntity passanger = this.getControllingPassenger();
            Vec3 view = this.calculateViewVector((float) (passanger.getViewXRot(1.0F)- 7.0F),passanger.getYHeadRot());
            BlockHitResult result = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(view.scale(30.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
            for (TowerPart<?> part : this.towers){
                BlockPos target = result.getBlockPos();
                BulletEntity arrow = new BulletEntity(this.level());
                arrow.setOwner(this);
                arrow.setPos(part.position());
                arrow.shoot(target.getX()-part.getX(),target.getY()-part.getY(),target.getZ()-part.getZ(),1.5F,1.0F);
                this.level().addFreshEntity(arrow);
                this.level().playSound(null,this,CVNSounds.RAY_TURRET_SHOOT3.get(),SoundSource.NEUTRAL,1.0F,1);

            }
        }
    }


    @Override
    protected float nextStep() {
        return super.nextStep();
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {

    }

    private void tickPart(TowerPart<?> p_31116_, double p_31117_, double p_31118_, double p_31119_) {
        p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
    }

    public void upEntities(List<LivingEntity> list){
        for (LivingEntity livingEntity : list){
            if(!this.is(livingEntity) && livingEntity.hurt(this.damageSources().mobAttack(this),25.0F) ){
                double dx = livingEntity.getX() - this.getX();
                double dz = livingEntity.getZ() - this.getZ();
                double normalize = dx * dx + dz * dz;
                livingEntity.push(dx/normalize,1,dz/normalize);
            }
        }
    }

    protected void updateWalkAnimation(float p_268362_) {
        float f;
        if (this.getPose() == Pose.STANDING && !this.isInWater() && this.onGround()) {
            f = Math.min(p_268362_ * 6.0F, 1.0F);
        } else {
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
                this.inWater.start(this.tickCount);
                this.idleAnimationTimeout=0;
            } else {
                --this.swimAnimationTimeout;
            }
        }else {
            if(this.onGround()){
                if (this.idleAnimationTimeout <= 0) {
                    this.idleAnimationTimeout = 80;
                    this.inWater.stop();
                    this.stomp.stop();
                    this.meleeAttack.stop();
                    this.idle.start(this.tickCount);
                    this.swimAnimationTimeout=0;
                } else {
                    --this.idleAnimationTimeout;
                }
            }else {
                this.idle.stop();
            }
        }


        if(this.smokeEffectTimer>0){
            this.smokeEffectTimer--;
            Vec3 head = this.getHeadPos();
            for(int j = 0 ; j<3 ; j++){
                this.level().addParticle(ParticleTypes.SMOKE,head.x,head.y,head.z,0.0F,0.0F,0.0F);
            }
        }

        if(this.interpolationCamTimer>0){
            this.interpolationCamTimer--;
        }

        if(this.prepareLaserTimer==12){
            this.level().playLocalSound(this.getX(),this.getY(),this.getZ(),CVNSounds.RAY_CHARGE_LASER.get(),SoundSource.NEUTRAL,10.0F,1.0F,false);
        }

        if(Minecraft.getInstance().player!=null && Minecraft.getInstance().player.getVehicle()==this){
            if(Minecraft.getInstance().options.keySprint.isDown()){
                PacketHandler.sendToServer(new PacketKeySync(2));
            }
        }

        if(!this.onGround() && !this.isInWater()){
            this.isAir.start(this.tickCount);
        }else {
            this.isAir.stop();
        }

        if(this.restoreCracking>0){
            this.restoreCracking--;
            if(this.restoreCracking==0){
                this.crackingBlock.clear();
            }
        }

        this.tickStep();

    }

    public void serverTick(){

        if (this.tickCount % 5 == 0) {
            if (this.positionOld != null && this.positionOld.closerThan(this.position(), 0.01)) {
                this.setSprinting(false);
            }
            this.positionOld = this.position();
        }
        if(this.isSprinting()){
            this.knockBack(this.level().getEntities(this,this.body.getBoundingBox().inflate(0.1F),e->!this.is(e)),false);
        }
    }
    public int getAmount(){
        return this.entityData.get(AMOUNT);
    }
    public void setAmount(int amount){
        this.entityData.set(AMOUNT,amount);
    }

    public void plusAmount(int i){
        this.setAmount(this.getAmount()+i);
    }

    public void setOwnerId(UUID uuid){
        this.entityData.set(OWNER_ID,Optional.ofNullable(uuid));
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
        this.entityData.define(OWNER_ID,Optional.empty());
        this.entityData.define(SITTING,false);
        this.entityData.define(ATTACKING,false);
        this.entityData.define(BLADE_ON,false);
        this.entityData.define(TOWER_ON,false);
        this.entityData.define(LASER,false);
        this.entityData.define(AMOUNT,0);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSitting() || (this.isAttacking() && !this.bladeOn());
    }

    private boolean isOrderedToSit() {
        return this.isSitting();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putInt("amount",this.getAmount());
        p_21484_.putBoolean("onBlade",this.bladeOn());
        p_21484_.putBoolean("onTower",this.towerOn());
        if(this.getOwnerUUID()!=null){
            p_21484_.putUUID("Owner",this.getOwnerUUID());
        }
        p_21484_.putBoolean("sitting",this.isSitting());

        ListTag listtag = new ListTag();
        if(this.inventory!=null){
            for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte)i);
                    itemstack.save(compoundtag);
                    listtag.add(compoundtag);
                }
            }

            p_21484_.put("Items", listtag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        if(this.inventory==null){
            this.createInventory();
        }
        if(this.inventory!=null){
            ListTag listtag = p_21450_.getList("Items", 10);

            for(int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                int j = compoundtag.getByte("Slot") & 255;
                if (j < this.inventory.getContainerSize()) {
                    this.inventory.setItem(j, ItemStack.of(compoundtag));
                }
            }
        }
        super.readAdditionalSaveData(p_21450_);
        this.setAmount(p_21450_.getInt("amount"));
        this.setTowerOn(p_21450_.getBoolean("onTower"));
        this.setBladeOn(p_21450_.getBoolean("onBlade"));
        UUID uuid;
        if (p_21450_.hasUUID("Owner")) {
            uuid = p_21450_.getUUID("Owner");
        } else {
            String s = p_21450_.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerId(uuid);
            } catch (Throwable ignored) {

            }
        }
        this.setSitting(p_21450_.getBoolean("sitting"));

    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.setIsAttacking(true);
            this.idle.stop();
            this.attackTimer = 40;
            this.idleAnimationTimeout = 40;
            this.stomp.start(this.tickCount);
        }if(p_21375_==5){
            this.setIsAttacking(true);
            this.attackTimer = 35;
            this.idle.stop();
            this.idleAnimationTimeout=35;
            this.meleeAttack.start(this.tickCount);
        }else if(p_21375_==8){
            this.setTowerOn(false);
            this.towerOff.start(this.tickCount);
        }else if(p_21375_==12){
            this.setTowerOn(true);
            this.towerOn.start(this.tickCount);
        }else if(p_21375_==9){
            this.setBladeOn(false);
            this.bladeOff.start(this.tickCount);
        }else if(p_21375_==11){
            this.setBladeOn(true);
            this.bladeOn.start(this.tickCount);
        }else if(p_21375_==13){
            this.prepareLaser.start(this.tickCount);
            this.prepareLaserTimer = 58;
        }else if(p_21375_==14){
            this.setLaser(true);
        }else if(p_21375_==15){
            if(this.towerOn()){
                this.towerOn.stop();
                this.towerOff.start(this.tickCount);
            }else {
                this.towerOff.stop();
                this.towerOn.start(this.tickCount);
            }
            this.setTowerOn(!this.towerOn());
        }else if(p_21375_==16){
            if(this.bladeOn()){
                this.bladeOn.stop();
                this.bladeOff.start(this.tickCount);
            }else {
                this.bladeOff.stop();
                this.bladeOn.start(this.tickCount);
            }
            this.setBladeOn(!this.bladeOn());
        }else if(p_21375_==17){
            this.cooldownLaser=600;
            this.smokeEffectTimer=100;
            this.interpolationCamTimer = 10;
            this.interpolationCamTimer0 = 10;
            this.setLaser(false);
            this.idleAnimationTimeout=0;
            this.laser.stop();
            this.laserPosition = null;
        }else if(p_21375_==71){
            this.plusAmount(-3);
            this.tickReload=10;
        }else if(p_21375_==72){
            this.level().playLocalSound(this.blockPosition(),CVNSounds.RAY_MISSILE_RELOAD.get(),SoundSource.NEUTRAL,20.0F,1.0F,true);
        }
        super.handleEntityEvent(p_21375_);
    }

    @Override
    public void onPlayerJump(int p_30591_) {
        if(cooldownJump<=0){
            if (p_30591_ < 0) {
                p_30591_ = 0;
            }


            if (p_30591_ >= 90) {
                this.jumpPendingScale = 1.0F;
            } else {
                this.jumpPendingScale = 0.4F + 0.4F * (float)p_30591_ / 90.0F;
            }
            this.cooldownJump = 40;
        }
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int p_21695_) {
        this.playSound(CVNSounds.RAY_JUMP.get(), 30.0F, 1.0F);
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
        if(key == 1){
            if(this.getAmount()>0 && this.tickReload<=0){
                int i = 0;
                List<LivingEntity> list = new ArrayList<>();
                BlockHitResult blockEnd = this.level().clip(new ClipContext(this.getHeadPos(),this.getHeadPos().add(this.calculateViewVector(this.getControllingPassenger().getViewXRot(1.0F),this.getControllingPassenger().getYHeadRot()).scale(100.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this));
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(this.level(),this,this.getHeadPos(),this.getHeadPos().add(this.calculateViewVector(this.getControllingPassenger().getViewXRot(1.0F),this.getControllingPassenger().getYHeadRot()).scale(100.0D)),this.getBoundingBox().inflate(100.0F), e->!this.is(e),0.5F);
                if(hit != null){
                    Vec3 target = hit.getEntity().position();
                    list = hit.getEntity().level().getEntitiesOfClass(LivingEntity.class,hit.getEntity().getBoundingBox().inflate(5.0F),e->!this.is(e));
                    list.sort(Comparator.comparingDouble(e -> e.distanceToSqr(target.x, target.y, target.z)));
                }else if (blockEnd.getType() != HitResult.Type.MISS){
                    Vec3 target = blockEnd.getBlockPos().getCenter();
                    list = new ArrayList<>(
                            this.level().getEntitiesOfClass(LivingEntity.class, new AABB(blockEnd.getBlockPos()).inflate(5.0F), e -> !this.is(e))
                    );
                    list.sort(Comparator.comparingDouble(e -> e.distanceToSqr(target.x, target.y, target.z)));
                }
                this.tickReload = 10;
                if(!list.isEmpty()){
                    this.plusAmount(-3);
                    this.level().broadcastEntityEvent(this,(byte) 71);
                    cooldownAmount=200;
                    int k = list.size()>2 ? 1 : 4-list.size();
                    for (LivingEntity living : list){
                        for(int j=0 ; j<k ; j++){
                            this.spawnMissile(living,i);
                            if(i>2){
                                break;
                            }
                            i++;
                        }
                        if(i>2){
                            break;
                        }
                    }
                }
            }
        }else if(key==0){
            if(!this.isInWater() && !this.isAttacking() && this.onGround()){
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
                    if(this.prepareLaserTimer<=0 && this.cooldownLaser<=0){
                        if(this.isLaser()){
                            this.level().playSound(null,this, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,2.0F,1.0F);
                            this.setLaser(false);
                            this.cooldownLaser=600;
                            this.level().broadcastEntityEvent(this,(byte) 17);
                        }else {
                            this.level().broadcastEntityEvent(this,(byte) 13);
                            this.prepareLaserTimer = 58;
                        }
                    }
                }
            }
        }else if(MGKeybinds.attackKey3.getKey().getValue()==key){
            if(!this.isAttacking()){
                if(this.bladeOn()){
                    this.level().playSound(null,this, SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL,2.0F,-2.0F);
                }else {
                    this.level().playSound(null,this, SoundEvents.PISTON_EXTEND, SoundSource.NEUTRAL,2.0F,-2.0F);
                }
                this.setBladeOn(!this.bladeOn());
                this.level().broadcastEntityEvent(this,(byte) 16);
            }

        }else {
            if(this.towerOn()){
                this.level().playSound(null,this, SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL,2.0F,-2.0F);
            }else {
                this.level().playSound(null,this, SoundEvents.PISTON_EXTEND, SoundSource.NEUTRAL,2.0F,-2.0F);
            }
            this.setTowerOn(!this.towerOn());
            this.level().broadcastEntityEvent(this,(byte) 15);
        }
    }

    public void spawnMissile(LivingEntity living,int i){
        MissileEntity missile = new MissileEntity(this.level(),i + 1);
        missile.setOwner(this);
        missile.setTarget(living);

        missile.setPos(this.slots[i].getX(),this.slots[i].getY(),this.slots[i].getZ());
        missile.calculateRotPosition(missile.blockPosition(),living.blockPosition());
        this.level().addFreshEntity(missile);
        PacketHandler.sendToAllTracking(new PacketMissileTarget(missile.getId(),living.getId(),i + 1),this);
    }

    @Override
    public void containerChanged(Container p_18983_) {

    }

    @Override
    public void openCustomInventoryScreen(Player p_217023_) {
        if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(p_217023_))) {
            this.openInventory((ServerPlayer) p_217023_, this.inventory);
        }
    }

    public void openInventory(ServerPlayer player, Container p_9060_) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        player.nextContainerCounter();
        player.connection.send(new ClientboundOpenScreenPacket(player.containerCounter, MenuType.GENERIC_9x6, Component.literal("Ray Inventory")));
        player.containerMenu = ChestMenu.sixRows(player.containerCounter, player.getInventory(), p_9060_);
        player.initMenu(player.containerMenu);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.containerMenu));
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.inventory != null) {
            for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);
                if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                    this.spawnAtLocation(itemstack);
                }
            }

        }
    }

    protected void createInventory() {
        SimpleContainer simplecontainer = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if (simplecontainer != null) {
            simplecontainer.removeListener(this);
            int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

            for(int j = 0; j < i; ++j) {
                ItemStack itemstack = simplecontainer.getItem(j);
                if (!itemstack.isEmpty()) {
                    this.inventory.setItem(j, itemstack.copy());
                }
            }
        }

        this.inventory.addListener(this);
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.inventory));
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.core.Direction facing) {
        if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && itemHandler != null && this.isAlive())
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemHandler != null) {
            net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
            itemHandler = null;
            oldHandler.invalidate();
        }
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_ID).orElse(null);
    }
    static class OwnerHurtTargetMetalGearGoal extends TargetGoal {
        private final MetalGearRayEntity tameAnimal;
        private LivingEntity ownerLastHurt;
        private int timestamp;

        public OwnerHurtTargetMetalGearGoal(MetalGearRayEntity p_26114_) {
            super(p_26114_, false);
            this.tameAnimal = p_26114_;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        public boolean canUse() {
            if (this.tameAnimal.getOwnerUUID()!=null) {
                LivingEntity livingentity = this.tameAnimal.getOwner();
                if (livingentity == null) {
                    return false;
                } else {
                    this.ownerLastHurt = livingentity.getLastHurtMob();
                    int i = livingentity.getLastHurtMobTimestamp();
                    return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
                }
            } else {
                return false;
            }
        }

        public void start() {
            this.mob.setTarget(this.ownerLastHurt);
            LivingEntity livingentity = this.tameAnimal.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtMobTimestamp();
            }

            super.start();
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
            return !MetalGearRayEntity.this.isVehicle() && super.canUse()  && MetalGearRayEntity.this.prepareLaserTimer<=0 && !MetalGearRayEntity.this.isVehicle() && !MetalGearRayEntity.this.isLaser();
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
                int nextStrat = MetalGearRayEntity.this.cooldownLaser<=0 && MetalGearRayEntity.this.isValidTarget(MetalGearRayEntity.this.getTarget()) ? MetalGearRayEntity.this.random.nextInt(0,1) : 1;
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

    public static class FollowOwnerGoal extends Goal {
        private final MetalGearRayEntity tamable;
        private LivingEntity owner;
        private final LevelReader level;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;
        private final boolean canFly;

        public FollowOwnerGoal(MetalGearRayEntity p_25294_) {
            this.tamable = p_25294_;
            this.level = p_25294_.level();
            this.speedModifier = 2.0F;
            this.navigation = p_25294_.getNavigation();
            this.startDistance = 40;
            this.stopDistance = 7;
            this.canFly = false;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            if (!(p_25294_.getNavigation() instanceof GroundPathNavigation) && !(p_25294_.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.unableToMove()) {
                return false;
            } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.unableToMove()) {
                return false;
            } else {
                return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
            }
        }

        private boolean unableToMove() {
            return this.tamable.isOrderedToSit() || this.tamable.isPassenger() || this.tamable.isLeashed();
        }

        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
            this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        public void tick() {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.tamable.distanceToSqr(this.owner) >= 900.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = this.owner.blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean maybeTeleportTo(int p_25304_, int p_25305_, int p_25306_) {
            if (Math.abs((double)p_25304_ - this.owner.getX()) < 2.0D && Math.abs((double)p_25306_ - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(p_25304_, p_25305_, p_25306_))) {
                return false;
            } else {
                this.tamable.moveTo((double)p_25304_ + 0.5D, (double)p_25305_, (double)p_25306_ + 0.5D, this.tamable.getYRot(), this.tamable.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos p_25308_) {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, p_25308_.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(p_25308_.below());
                if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = p_25308_.subtract(this.tamable.blockPosition());
                    return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockpos));
                }
            }
        }

        private int randomIntInclusive(int p_25301_, int p_25302_) {
            return this.tamable.getRandom().nextInt(p_25302_ - p_25301_ + 1) + p_25301_;
        }
    }


}
