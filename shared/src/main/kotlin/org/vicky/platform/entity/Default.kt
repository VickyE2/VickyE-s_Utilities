package org.vicky.platform.entity

import org.vicky.platform.PlatformPlayer
import org.vicky.platform.entity.LookAtAttacker as BBLookAtAttacker
import org.vicky.platform.PlatformPlugin
import org.vicky.platform.utils.ResourceLocation

fun rl(key: String, location: String): ResourceLocation =
    ResourceLocation.from(key, location)

fun rl(parsable: String): ResourceLocation =
    ResourceLocation.from(parsable)

infix fun String.rli(location: String): ResourceLocation =
    ResourceLocation.from(this, location)

fun interface ProducerIntendedTask {
    /** This functional interface should always base its main id on the [self] entity
     * ie:
     * ```
     * TaskBuilder.random(
     *     self,
     *     ResourceLocation.from("core", "look_at_nearest_player_${self.uuid}"),
     * )
     * .withRange(...)
     * ```
     *
     * This avoids task collisions when multiple entities execute
     * the same logical behavior simultaneously.
     */
    fun produce(self: PlatformLivingEntity, params: Map<String, Any>): CompiledTask
}

object DefaultTasks {
    object LookAtNearestPlayer : ProducerIntendedTask {
        override fun produce(self: PlatformLivingEntity, params: Map<String, Any>): CompiledTask = TaskBuilder.random(
            self,
            ResourceLocation.from("core", "look_at_nearest_player_${self.uuid}"),
            TaskLifecycle.REPEATING
        )
            .cooldownTicks((params["cooldown"] as Int?) ?: 120)
            .withRange(ResourceLocation.from("look_at_nearest_player", "find_closest_entity_${self.uuid}"), self.lookDistance)
                .filter(PlayersOnly)
                .withSingleResult()
            .performOnTarget(ResourceLocation.from("look_at_nearest_player", "set_target_${self.uuid}"))
                .doingTimed(SetTargetToLookAt, 60, runBlocking = false)
                .end()
            .build()
    }
    object LookAtAttacker : ProducerIntendedTask {
        override fun produce(self: PlatformLivingEntity, params: Map<String, Any>): CompiledTask = TaskBuilder.conditioned(
            self,
            ResourceLocation.from("core", "look_at_attacker_${self.uuid}"),
        )
            .performOnSelf(ResourceLocation.from("look_at_attacker", "set_target_${self.uuid}"))
                .doingTimed(BBLookAtAttacker, 40, runBlocking = false)
                .end()
            .build()
    }

    /**
     * A task to make the entity involved move around randomly
     * The params specifiable are: [[cooldown]], [[range]]
     */
    object PassiveWander : ProducerIntendedTask {
        override fun produce(self: PlatformLivingEntity, params: Map<String, Any>): CompiledTask =
            TaskBuilder.random(self, ResourceLocation.from("core", "wander_${self.uuid}"), TaskLifecycle.REPEATING, priority = 0)
                .blockMode()
                .cooldownTicks((params["cooldown"] as Int?) ?: 60)
                .withBlockRange(ResourceLocation.from("core", "find_block_${self.uuid}"),
                    range = (params["range"] as Double?) ?: 12.0)
                    .filter(BlockIsWalkableFilter)
                    .filter(BlockIsHighest)
                    .withRandomSingleResult()
                .performOnBlockTarget(ResourceLocation.from("core", "walk_to_block_${self.uuid}"))
                    .doingTimedBlock(WalkToBlock, runBlocking = false)
                    .end()
                .build()
    }
}

object DefaultHandlers {
    val MobDefaultHandler : PlatformEntityFactory.RegisteredMobEntityEventHandler =
        PlatformPlugin.entityFactory().registerHandler(
            "core" rli "default_mob_event_handler",
            object: MobEntityEventHandler {
                override fun onAttacked(self: PlatformLivingEntity, attacker: PlatformLivingEntity): EventResult {
                    self.setLastHurtByMob(attacker)
                    return EventResult.CONSUME
                }

                override fun onLeaveCombat(self: PlatformLivingEntity) {
                    self.setLastHurtByMob(null)
                    self.setLastHurtMob(null)
                }

                override fun onAttack(self: PlatformLivingEntity, victim: PlatformLivingEntity): EventResult {
                    if (victim is PlatformPlayer) {
                        self.setLastHurtByPlayer(victim)
                    }
                    else {
                        self.setLastHurtMob(victim)
                    }
                    return EventResult.CONSUME
                }
            }
        )
}