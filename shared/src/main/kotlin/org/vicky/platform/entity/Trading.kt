package org.vicky.platform.entity

import java.util.concurrent.ConcurrentHashMap
import org.vicky.platform.utils.ResourceLocation as RL

data class TradeOffer(
    val id: RL,
    val cost: List<Pair<RL, Int>>,     // list of (item id, amount) required
    val result: Pair<RL, Int>,         // (item id, amount) granted
    val maxUses: Int = Int.MAX_VALUE,
    val uses: Int = 0,
    val metadata: Map<String, Any> = emptyMap()
) {
    fun isExhausted() = uses >= maxUses
    fun withIncrementedUse() = copy(uses = uses + 1)
}

data class TradeSpec(
    val id: RL,
    val displayName: String,
    val offers: MutableList<TradeOffer> = mutableListOf()
)

object TradeRegistry {
    private val specs = ConcurrentHashMap<RL, TradeSpec>()
    fun register(spec: TradeSpec) { specs[spec.id] = spec }
    fun get(id: RL) = specs[id]
}