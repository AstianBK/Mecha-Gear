package com.TBK.metal_gear_ray.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.Random;

public class BeamExplosionParticles extends WaterDropParticle{
    protected BeamExplosionParticles(ClientLevel p_108484_, double p_108485_, double p_108486_, double p_108487_, double xSpeed, double ySpeed, double zSpeed ) {
        super(p_108484_, p_108485_, p_108486_, p_108487_);
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        this.xd += xSpeed;
        this.yd += ySpeed;
        this.zd += zSpeed;
        this.scale(new Random().nextFloat()*5+4.0F);
        this.lifetime=20;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ > this.lifetime) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BeamExplosionParticles bk_particles=new BeamExplosionParticles(world,x,y,z,xSpeed,ySpeed,zSpeed);
            bk_particles.pickSprite(this.spriteSet);
            return bk_particles;
        }
    }
}
