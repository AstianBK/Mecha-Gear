package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletEntity extends AbstractArrow {
    public BulletEntity(Level p_37249_) {
        super(CVNEntityType.BULLET.get(), p_37249_);
    }

    public BulletEntity(EntityType<BulletEntity> bulletEntityEntityType, Level level) {
        super(bulletEntityEntityType,level);
    }

    @Override
    protected void onHit(HitResult p_37260_) {
        super.onHit(p_37260_);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        Entity entity = p_37259_.getEntity();
        if((this.getOwner() == null || !this.getOwner().is(entity))){
            entity.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()),2.0F);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

}
