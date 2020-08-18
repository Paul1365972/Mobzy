package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.mobzy.ecs.components.minecraft.MobComponent
import com.mineinabyss.mobzy.mobzy
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import org.bukkit.Bukkit
import org.clapper.util.misc.SparseArrayList
import kotlin.reflect.KClass

typealias ComponentClass = KClass<out MobzyComponent>
object Engine {
    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mobzy, {
            registeredSystems.filter { it.interval == 1 || Bukkit.getCurrentTick() % it.interval == 0 }.forEach(TickingSystem::tick)
        }, 1, 1)
    }

    private var currId = 0

    fun getNextId() = ++currId

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    private val registeredSystems = mutableSetOf<TickingSystem>()

    fun addSystem(system: TickingSystem) = registeredSystems.add(system)

    fun addSystems(vararg systems: TickingSystem) = registeredSystems.addAll(systems)

    private val components = mutableMapOf<ComponentClass, SparseArrayList<MobzyComponent>>()
    private val bitsets = mutableMapOf<ComponentClass, BitVector>()

    fun getComponentFor(kClass: ComponentClass, id: Int) = components[kClass]?.get(id)
    fun hasComponentFor(kClass: ComponentClass, id: Int) = bitsets[kClass]?.contains(id) ?: false

    inline fun <reified T : MobzyComponent> get(id: Int): T? = getComponentFor(T::class, id) as? T
    inline fun <reified T : MobzyComponent> has(id: Int) = hasComponentFor(T::class, id)

    fun addComponent(id: Int, component: MobzyComponent) {
        components.getOrPut(component::class, { SparseArrayList() })[id] = component
        bitsets.getOrPut(component::class, { bitsOf() }).set(id)
    }

    fun getBitsMatching(vararg components: ComponentClass): BitVector? {
        return components.map { (bitsets[it] ?: return null).copy() }
                .reduce { a, b -> a.and(b).let { a } }
    }

    fun getComponentForId(component: ComponentClass, id: Int): MobzyComponent? {
        return components[component]?.get(id)
    }

    //TODO support component families with infix functions
    inline fun runFor(vararg components: KClass<out MobzyComponent>, run: (List<MobzyComponent>) -> Unit) {
        getBitsMatching(*components)?.forEachBit { index ->
            components.map { getComponentForId(it, index) ?: return@forEachBit }.apply(run)
        }
        0b1 and 0b1
    }

    //TODO clean up and expand for more parameters
    inline fun <reified T : MobzyComponent> runFor(run: (T) -> Unit) {
        val tClass = T::class
        getBitsMatching(tClass)?.forEachBit { index ->
            (getComponentForId(tClass, index) as? T)?.apply(run)
        }
    }

    inline fun <reified T : MobzyComponent, reified T2 : MobzyComponent> runFor(run: (T, T2) -> Unit) {
        val tClass = T::class
        val t2Class = T2::class
        getBitsMatching(tClass, t2Class)?.forEachBit { index ->
            run(getComponentForId(tClass, index) as T, getComponentForId(t2Class, index) as T2)
        }
    }
}