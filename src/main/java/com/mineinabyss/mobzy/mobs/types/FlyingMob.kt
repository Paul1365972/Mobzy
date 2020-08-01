package com.mineinabyss.mobzy.mobs.types

import com.mineinabyss.mobzy.api.nms.aliases.NMSDataContainer
import com.mineinabyss.mobzy.api.nms.aliases.NMSEntityLiving
import com.mineinabyss.mobzy.api.nms.aliases.NMSEntityType
import com.mineinabyss.mobzy.api.nms.aliases.NMSWorld
import com.mineinabyss.mobzy.api.pathfindergoals.addPathfinderGoal
import com.mineinabyss.mobzy.api.pathfindergoals.addTargetSelector
import com.mineinabyss.mobzy.mobs.CustomMob
import com.mineinabyss.mobzy.pathfinders.TargetAttackerGoal
import com.mineinabyss.mobzy.pathfinders.flying.FlyDamageTargetGoal
import com.mineinabyss.mobzy.pathfinders.flying.IdleFlyGoal
import com.mineinabyss.mobzy.registration.MobzyTemplates
import net.minecraft.server.v1_16_R1.*

/**
 * Lots of code taken from the EntityGhast class for flying mobs
 */
abstract class FlyingMob(world: NMSWorld, name: String) : MobzyEntityFlying(world, TODO()), CustomMob {
    override fun createPathfinders() {
        entity.persistentDataContainer
        addPathfinderGoal(1, PathfinderGoalFloat(this))
        addPathfinderGoal(2, FlyDamageTargetGoal(this))
        addPathfinderGoal(5, IdleFlyGoal(this))
        addPathfinderGoal(1, TargetAttackerGoal(this, 100.0))//TODO look at target selectors
        addTargetSelector(2, PathfinderGoalNearestAttackableTarget(this, EntityHuman::class.java, true))
    }

    init {
        createFromBase()
        addScoreboardTag("flyingMob")
        entity.removeWhenFarAway = true
        moveController = ControllerMoveFlying(this, 20, true) /*MZControllerMoveFlying(this)*/
    }
}