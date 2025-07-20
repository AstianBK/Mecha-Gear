package com.TBK.metal_gear_ray;


import com.TBK.metal_gear_ray.server.world.SpawnBiomeData;

public class DefaultBiomes {
    public static final SpawnBiomeData EMPTY = new SpawnBiomeData();

    public static final SpawnBiomeData RAY = new SpawnBiomeData()
            .addBiomeEntry(SpawnBiomeData.BiomeEntryType.REGISTRY_NAME, false, "minecraft:stony_peaks", 0)
            .addBiomeEntry(SpawnBiomeData.BiomeEntryType.REGISTRY_NAME, false, "minecraft:frozen_peaks", 1)
            .addBiomeEntry(SpawnBiomeData.BiomeEntryType.REGISTRY_NAME, false, "minecraft:jagged_peaks", 2);


}
