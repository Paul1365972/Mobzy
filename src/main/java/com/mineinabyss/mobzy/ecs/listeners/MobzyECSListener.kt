package com.mineinabyss.mobzy.ecs.listeners

import com.mineinabyss.geary.ecs.components.addComponent
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.minecraft.components.MobComponent
import com.mineinabyss.mobzy.api.nms.aliases.toNMS
import com.mineinabyss.mobzy.api.pathfindergoals.addPathfinderGoal
import com.mineinabyss.mobzy.api.pathfindergoals.addTargetSelector
import com.mineinabyss.mobzy.ecs.components.initialization.pathfinding.Pathfinders
import com.mineinabyss.mobzy.ecs.events.MobLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object MobzyECSListener : Listener {
    @EventHandler
    fun attachPathfindersOnEntityLoadedEvent(event: MobLoadEvent) {
        val (entity) = event
        val (mob) = entity.get<MobComponent>() ?: return
        val (targets, goals) = entity.get<Pathfinders>() ?: return

        targets?.forEach { (priority, component) ->
            mob.toNMS().addTargetSelector(priority.toInt(), component.build(mob))

            entity.addComponent(component)
        }
        goals?.forEach { (priority, component) ->
            mob.toNMS().addPathfinderGoal(priority.toInt(), component.build(mob))
            entity.addComponent(component)
        }
    }
}
