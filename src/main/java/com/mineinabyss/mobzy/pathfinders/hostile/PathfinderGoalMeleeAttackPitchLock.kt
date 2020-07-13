package com.mineinabyss.mobzy.pathfinders.hostile

import com.mineinabyss.mobzy.mobs.CustomMob
import com.mineinabyss.mobzy.pathfinders.MobzyPathfinderGoal

class PathfinderGoalMeleeAttackPitchLock(override val mob: CustomMob, private val speed: Double = 1.0) : MobzyPathfinderGoal() {
    override fun shouldExecute(): Boolean = target != null

    override fun shouldKeepExecuting(): Boolean = shouldExecute()

    override fun executeWhenCooledDown() {
        restartCooldown()
        val target = target ?: return
        mob.lookAtPitchLock(target)

        if (mob.canReach(target))
            target.damage(mob.template.attackDamage ?: 0.0, entity)

        navigation.moveToEntity(target, speed)
    }
}