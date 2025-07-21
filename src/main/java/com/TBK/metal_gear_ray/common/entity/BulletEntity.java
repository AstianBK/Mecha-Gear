package com.TBK.metal_gear_ray.common.entity;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.register.CVNEntityType;
import net.minecraft.core.BlockPos;
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
    public BulletEntity(Level p_37249_) {
        super(CVNEntityType.BULLET.get(), p_37249_);
        this.setNoGravity(true);
    }

    public BulletEntity(EntityType<BulletEntity> bulletEntityEntityType, Level level) {
        super(bulletEntityEntityType,level);
        this.setNoGravity(true);
    }

    @Override
    protected void onHit(HitResult p_37260_) {
        HitResult.Type hitresult$type = p_37260_.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)p_37260_);
            //this.level().gameEvent(GameEvent.PROJECTILE_LAND, p_37260_.getLocation(), GameEvent.Context.of(this, (BlockState)null));
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult)p_37260_;
            this.onHitBlock(blockhitresult);
            BlockPos blockpos = blockhitresult.getBlockPos();
            //this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        Entity entity = p_37259_.getEntity();
        if((this.getOwner() == null || !this.getOwner().is(entity))){
            entity.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()),2.0F);
            entity.invulnerableTime=0;
        }
    }





    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

}
