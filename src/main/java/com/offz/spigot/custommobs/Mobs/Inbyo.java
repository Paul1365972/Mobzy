package com.offz.spigot.custommobs.Mobs;

import com.offz.spigot.custommobs.Behaviours.AnimationBehaviour;
import com.offz.spigot.custommobs.Mobs.Type.MobType;
import net.minecraft.server.v1_13_R2.EntityZombie;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Inbyo extends EntityZombie {
    public Inbyo(World world) {
        super(world);
        Zombie inbyo = (Zombie) this.getBukkitEntity();


        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(this, Neritantan.class, true));

        this.addScoreboardTag("customMob");
        inbyo.setCustomName("Inbyo");
        this.setCustomNameVisible(false);
        this.setSilent(true);
        inbyo.setRemoveWhenFarAway(true);

        inbyo.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true));

        MobType type = MobType.getRegisteredMobType(inbyo);
        AnimationBehaviour.registerMob(inbyo, type, type.getModelID());

        this.getWorld().addEntity(this);
    }

    @Override
    public void k() {
        super.k();
        this.getBukkitEntity().setFireTicks(0);

        this.setBaby(false); //TODO: SOMETIMES SPAWNS AS A BABY EVEN WITH THIS
        ((Zombie) this.getBukkitEntity()).getEquipment().clear();
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(40.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.45);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7.0D);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(64.0D);
    }
}