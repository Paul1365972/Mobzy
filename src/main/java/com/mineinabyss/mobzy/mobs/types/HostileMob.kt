package com.mineinabyss.mobzy.mobs.types

import com.mineinabyss.mobzy.api.pathfindergoals.addPathfinderGoal
import com.mineinabyss.mobzy.api.pathfindergoals.addTargetSelector
import com.mineinabyss.mobzy.mobs.CustomMob
import com.mineinabyss.mobzy.pathfinders.WalkingAnimationGoal
import com.mineinabyss.mobzy.pathfinders.hostile.MeleeAttackGoal
import net.minecraft.server.v1_16_R1.*
import org.bukkit.entity.Creature


/**
 * Lots of code taken from EntityZombie
 */
abstract class HostileMob(world: World, name: String) : MobzyEntityMonster(world, TODO()), CustomMob {
    override fun createPathfinders() {
        addPathfinderGoal(0, WalkingAnimationGoal(entity, template.model))
        addPathfinderGoal(2, MeleeAttackGoal(entity as Creature, attackSpeed = 1.0, seeThroughWalls = false))
        addPathfinderGoal(3, PathfinderGoalFloat(this))
        addPathfinderGoal(7, PathfinderGoalRandomStrollLand(this, 1.0))
        addPathfinderGoal(7, PathfinderGoalLookAtPlayer(this, EntityPlayer::class.java, 8.0f))
        addPathfinderGoal(8, PathfinderGoalRandomLookaround(this))

        addTargetSelector(2, PathfinderGoalNearestAttackableTarget(this, EntityHuman::class.java, true))
    }

    /** Removes entity if not in peaceful mode */
    override fun tick() = super.tick().also { if (!world.isClientSide && world.difficulty == EnumDifficulty.PEACEFUL) die() }

    init {
        createFromBase()
        addScoreboardTag("hostileMob")
        entity.removeWhenFarAway = true
        attributeMap
    }
}