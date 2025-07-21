package com.TBK.metal_gear_ray;

import com.TBK.metal_gear_ray.client.renderer.BulletRenderer;
import com.TBK.metal_gear_ray.client.renderer.MetalGearRayRenderer;
import com.TBK.metal_gear_ray.common.entity.BulletEntity;
import com.TBK.metal_gear_ray.common.network.PacketHandler;
import com.TBK.metal_gear_ray.common.register.*;
import com.TBK.metal_gear_ray.server.world.BKBiomeSpawn;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MetalGearRayMod.MODID)
public class MetalGearRayMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "metal_gear_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static double x=0;
    public static double y=0;
    public static double z=0;
    public static double xq=0;
    public static double yq=0;
    public static double zq=0;

    public MetalGearRayMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        MinecraftForge.EVENT_BUS.register(this);

        CVNItems.ITEMS.register(modEventBus);
        CVNEntityType.ENTITY_TYPES.register(modEventBus);
        MGParticles.PARTICLE_TYPES.register(modEventBus);
        CVNSounds.SOUND_EVENTS.register(modEventBus);
        final DeferredRegister<Codec<? extends BiomeModifier>> biomeModifiers = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MetalGearRayMod.MODID);
        biomeModifiers.register(modEventBus);
        biomeModifiers.register("metal_gear_archive_spawn", BKBiomeSpawn::makeCodec);
        CVNCreativeTabs.TABS.register(modEventBus);
        PacketHandler.registerMessages();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->{
            modEventBus.addListener(this::registerRenderers);
            modEventBus.addListener(this::onRegisterAdditionalModels);
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerRenderers(FMLCommonSetupEvent event){
        EntityRenderers.register(CVNEntityType.RAY.get(), MetalGearRayRenderer::new);
        EntityRenderers.register(CVNEntityType.BULLET.get(), BulletRenderer::new);
    }
    public void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(MODID, "obj/entity/ray_head.obj"));
    }
}
