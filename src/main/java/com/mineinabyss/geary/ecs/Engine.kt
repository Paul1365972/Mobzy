package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.removeChildren
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.mobzy.mobzy
import com.zaxxer.sparsebits.SparseBitSet
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.clapper.util.misc.SparseArrayList
import java.util.*
import kotlin.reflect.KClass

typealias ComponentClass = KClass<out MobzyComponent>

object Engine {
    val componentsKey = NamespacedKey("geary", "components")

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mobzy, {
            registeredSystems.filter { it.interval == 1 || Bukkit.getCurrentTick() % it.interval == 0 }.forEach(TickingSystem::tick)
        }, 1, 1)
    }

    private var currId = 0

    //TODO there's likely a more performant option
    private val removedEntities = Stack<Int>()

    @Synchronized
    fun getNextId(): Int = if (removedEntities.isNotEmpty()) removedEntities.pop() ?: ++currId else ++currId

    inline fun entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    private val registeredSystems = mutableSetOf<TickingSystem>()

    fun addSystem(system: TickingSystem) = registeredSystems.add(system)

    fun addSystems(vararg systems: TickingSystem) = registeredSystems.addAll(systems)

    //TODO get a more memory efficient list, right now this is literally just an ArrayList that auto expands
    private val components = mutableMapOf<ComponentClass, SparseArrayList<MobzyComponent>>()
    internal val bitsets = mutableMapOf<ComponentClass, BitVector>()

    fun getComponentsFor(id: Int) = components.mapNotNull { (_, value) -> value.getOrNull(id) }
    fun addComponentsFor(id: Int, components: Set<MobzyComponent>) = components.forEach {
        addComponent(id, it)
    }

    fun getComponentFor(kClass: ComponentClass, id: Int) = runCatching { components[kClass]?.get(id) }.getOrNull()
    fun hasComponentFor(kClass: ComponentClass, id: Int) = bitsets[kClass]?.contains(id) ?: false

    inline fun <reified T : MobzyComponent> get(id: Int): T? = getComponentFor(T::class, id) as? T
    inline fun <reified T : MobzyComponent> has(id: Int) = hasComponentFor(T::class, id)

    fun <T : MobzyComponent> addComponent(id: Int, component: T): T {
        components.getOrPut(component::class, { SparseArrayList() })[id] = component
        bitsets.getOrPut(component::class, { bitsOf() }).set(id)
        return component
    }

    inline fun <reified T : MobzyComponent> getOrDefault(id: Int, component: () -> T): T =
            get<T>(id) ?: addComponent(id, component())

    fun getBitsMatching(vararg components: ComponentClass, andNot: Array<out ComponentClass> = emptyArray()): BitVector? {
        val allowed = components
                .mapTo(mutableListOf()) { (bitsets[it] ?: return null) }
                .also { list -> list[0] = list[0].copy() } //only copy the first bitset, all further operations only mutate it
                .reduce { a, b -> a.and(b).let { a } }
        return if (andNot.isEmpty())
            allowed
        else andNot.mapNotNull { (bitsets[it]) }
                .fold(allowed) { a, b -> a.andNot(b).let { a } }
    }

    //TODO might be a smarter way of storing these as an implicit list within a larger list of entities eventually
    fun removeEntity(id: Int) {
        //clear itself from parent and children
        geary(id){
            parent = null
            removeChildren()
        }

        //clear all components
        bitsets.mapNotNull { (kclass, bitset) ->
            if (bitset[id]) {
                bitset[id] = false
                components[kclass]
            } else
                null
        }.forEach { it[id] = null }

        //add current id into queue for reuse
        removedEntities.push(id)
    }

    //TODO support component families with infix functions
    inline fun runFor(vararg components: ComponentClass, andNot: Array<out ComponentClass> = emptyArray(), run: GearyEntity.(List<MobzyComponent>) -> Unit) {
        getBitsMatching(*components, andNot = andNot)?.forEachBit { index ->
            geary(index).run(components.map { getComponentFor(it, index) ?: return@forEachBit })
        }
    }

    //TODO clean up and expand for more parameters
    inline fun <reified T : MobzyComponent> runFor(vararg andNot: ComponentClass = emptyArray(), run: GearyEntity.(T) -> Unit) {
        val tClass = T::class
        getBitsMatching(tClass, andNot = andNot)?.forEachBit { index ->
            geary(index).run(getComponentFor(tClass, index) as T)
        }
    }

    inline fun <reified T : MobzyComponent, reified T2 : MobzyComponent> runFor(vararg andNot: ComponentClass = emptyArray(), run: GearyEntity.(T, T2) -> Unit) {
        val tClass = T::class
        val t2Class = T2::class
        getBitsMatching(tClass, t2Class, andNot = andNot)?.forEachBit { index ->
            geary(index).run(getComponentFor(tClass, index) as T, getComponentFor(t2Class, index) as T2)
        }
    }
}

inline fun SparseBitSet.forEachBit(block: (Int) -> Unit) {
    var i = 0
    while (i >= 0) {
        i = nextSetBit(i + 1)
        block(i)
    }
}


