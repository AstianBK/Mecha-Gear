package com.TBK.metal_gear_ray.common.items;

import com.TBK.metal_gear_ray.MetalGearRayMod;
import com.TBK.metal_gear_ray.common.api.IArsenalPlayer;
import com.TBK.metal_gear_ray.common.entity.MetalGearRayEntity;
import com.TBK.metal_gear_ray.server.capability.ArsenalCapability;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DeployerItem extends Item {
    public DeployerItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        ArsenalCapability arsenal = ArsenalCapability.get(p_41427_.getPlayer());
        if(arsenal!=null){
            if(!arsenal.rayActive()){
                arsenal.spawnOrCreateRay(p_41427_.getPlayer(),p_41427_.getClickedPos());
            }else {
                if(!p_41427_.getLevel().isClientSide){
                    arsenal.checkRayState(p_41427_.getLevel());
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(p_41427_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {

        return super.use(p_41432_, p_41433_, p_41434_);
    }
}
