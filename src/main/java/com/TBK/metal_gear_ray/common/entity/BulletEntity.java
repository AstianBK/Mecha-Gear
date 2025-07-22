package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletEntity extends AbstractArrow {
    public int lifeTime = 0;
    public BulletEntity(Level p_37249_) {
        super(CVNEntityType.BULLET.get(), p_37249_);
        this.setNoGravity(true);
        this.lifeTime = 50;
    }

    public BulletEntity(EntityType<BulletEntity> bulletEntityEntityType, Level level) {
        super(bulletEntityEntityType,level);
        this.setNoGravity(true);
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

    @Override
    protected void onHit(HitResult p_37260_) {
        HitResult.Type hitresult$type = p_37260_.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)p_37260_);
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.lifeTime--<=0){
            this.discard();
        }
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
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

}
