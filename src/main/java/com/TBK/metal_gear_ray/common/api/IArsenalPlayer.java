package com.TBK.metal_gear_ray.common.api;

import com.TBK.metal_gear_ray.server.capability.ArsenalCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

public interface IArsenalPlayer extends INBTSerializable<CompoundTag> {
    void setPlayer(Player player);
    Player getPlayer();
    public void spawnOrCreateRay(Player player, BlockPos pos);
}
