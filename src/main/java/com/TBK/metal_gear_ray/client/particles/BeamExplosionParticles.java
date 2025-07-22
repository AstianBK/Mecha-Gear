package com.TBK.metal_gear_ray.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.Random;

public class BeamExplosionParticles extends ExplodeParticle {
    private final SpriteSet sprites;

    protected BeamExplosionParticles(ClientLevel p_108484_, double p_108485_, double p_108486_, double p_108487_, double xSpeed, double ySpeed, double zSpeed,SpriteSet spriteSet ) {
        super(p_108484_,p_108485_,p_108486_,p_108487_,xSpeed,ySpeed,zSpeed,spriteSet);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.scale(new Random().nextFloat()*5+4.0F);
        this.lifetime=40;
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BeamExplosionParticles bk_particles=new BeamExplosionParticles(world,x,y,z,xSpeed,ySpeed,zSpeed,this.spriteSet);
            bk_particles.pickSprite(this.spriteSet);
            return bk_particles;
        }
    }
}
