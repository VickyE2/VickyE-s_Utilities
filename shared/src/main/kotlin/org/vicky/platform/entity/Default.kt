/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

import org.vicky.platform.PlatformPlugin
import org.vicky.platform.entity.distpacher.Signals
import org.vicky.platform.player.PlatformPlayer
import org.vicky.platform.utils.ResourceLocation
import org.vicky.utilities.Pair
import org.vicky.platform.entity.LookAtAttacker as BBLookAtAttacker

fun rl(key: String, location: String): ResourceLocation =
    ResourceLocation.from(key, location)

fun rl(parsable: String): ResourceLocation =
    ResourceLocation.from(parsable)

infix fun String.rli(location: String): ResourceLocation =
    ResourceLocation.from(this, location)
infix fun <T, U> T.pair(value: U): Pair<T, U> =
    Pair(this, value)

fun String.minecraft(): ResourceLocation =
    ResourceLocation.from("minecraft", this)
fun String.minecraftString(): String =
    ResourceLocation.from("minecraft", this).toString()
fun String.core(): ResourceLocation =
    ResourceLocation.from("core", this)

object DefaultTasks {
    val SayToNearestPlayerTask : CompiledTask = TaskBuilder.random(
            ResourceLocation.from("core", "say_to_nearest_player"),
            TaskLifecycle.ONE_SHOT
        )
            .withEntityRange(ResourceLocation.from("nearest_player", "find_closest_player"), 20.0)
                .filter(PlayersOnly)
                .withSingleResult()
            .performOnEntityTarget(ResourceLocation.from("say_to_nearest_player", "set_target"))
                .doing(SayToTarget)
                .end()
            .build()

    val SayToPlayersInRangeTask : CompiledTask = TaskBuilder.random(
            ResourceLocation.from("core", "say_to_nearest_player"),
            TaskLifecycle.ONE_SHOT
        )
            .withEntityRange(ResourceLocation.from("nearest_player", "find_closest_player"), 20.0)
                .filter(PlayersOnly)
                .withMultipleResult()
            .performOnEntityTarget(ResourceLocation.from("say_to_nearest_player", "set_target"))
                .doing(SayToTarget)
                .end()
            .build()

    val SayToPlayersInWorldTask : CompiledTask  = TaskBuilder.random(
            ResourceLocation.from("core", "say_to_all_players"),
            TaskLifecycle.ONE_SHOT
        )
            .performOnSelf(ResourceLocation.from("say_to_all_players", "set_target"))
                .doing(SayToAllPlayersInWorld)
                .end()
            .build()

    val SayToAttackerTask : CompiledTask = TaskBuilder.random(
            ResourceLocation.from("core", "say_to_all_players"),
            TaskLifecycle.ONE_SHOT
        )
            .performOnSelf(ResourceLocation.from("say_to_all_players", "set_target"))
                .doing(SayToAttacker)
                .end()
            .build()

    val LookAtAttackerTask : CompiledTask = TaskBuilder.conditioned(
        ResourceLocation.from("core", "look_at_attacker")
    )
        .performOnSelf(ResourceLocation.from("look_at_attacker", "set_target"))
        .doingTimed(BBLookAtAttacker, 40, runBlocking = false, slot = DefaultSlots.LOOK)
        .end()
        .build()

    val LookAtNearestPlayerTask : CompiledTask = TaskBuilder.random(
            ResourceLocation.from("core", "look_at_nearest_player"),
            TaskLifecycle.REPEATING
        )
            .defaultCooldownTicks(120)
            .withEntityRange(ResourceLocation.from("look_at_nearest_player", "find_closest_entity"), 20.0)
                .filter(PlayersOnly)
                .withSingleResult()
            .performOnEntityTarget(ResourceLocation.from("look_at_nearest_player", "set_target"))
                .doingTimed(SetTargetToLookAt, 60, runBlocking = false, slot = DefaultSlots.LOOK)
                .end()
            .build()

    val LookAtAttackerTillOutOfCombatTask : CompiledTask = TaskBuilder.conditioned(
            ResourceLocation.from("core", "look_at_attacker")
        )
            .runUntilReceive(Signals.OUT_OF_COMBAT)
            .performOnSelf(ResourceLocation.from("look_at_attacker", "set_target"))
                .doingTimed(BBLookAtAttacker, 40, runBlocking = false, slot = DefaultSlots.LOOK)
                .end()
            .build()


    /**
     * A task to make the entity involved move around randomly
     * The params specifiable are: _[params.cooldown]_, _[params.range]_ _[params.priority]_
     */
    val PassiveWanderTask : CompiledTask = TaskBuilder.random(
            ResourceLocation.from("core", "wander"),
            TaskLifecycle.REPEATING
        )
            .blockMode()
            .defaultCooldownTicks(60)
            .withBlockRange(ResourceLocation.from("core", "find_block"), 12.0)
                .filter(BlockIsWalkableFilter)
                .filter(BlockIsHighest)
                .withRandomSingleResult()
            .performOnBlockTarget(ResourceLocation.from("core", "walk_to_block"))
                .doingTimedBlock(WalkToBlock, runBlocking = false, durationOverride = -10, slot = DefaultSlots.MOVE)
                .end()
            .build()

}

object DefaultHandlers {
    val MobDefaultHandler : PlatformEntityFactory.RegisteredMobEntityEventHandler =
        PlatformPlugin.entityFactory().registerHandler(
            "default_mob_event_handler".core(),
            object: DefaultTriggerMobEventHandler() {
                override fun onAttacked(self: PlatformLivingEntity, attacker: PlatformLivingEntity): EventResult {
                    self.setLastHurtByMob(attacker)
                    super.onAttacked(self, attacker)
                    return EventResult.CONSUME
                }

                override fun onLeaveCombat(self: PlatformLivingEntity) {
                    self.setLastHurtByMob(null)
                    self.setLastHurtMob(null)
                    super.onLeaveCombat(self)
                }

                override fun onAttack(self: PlatformLivingEntity, victim: PlatformLivingEntity): EventResult {
                    if (victim is PlatformPlayer) {
                        self.setLastHurtByPlayer(victim)
                    }
                    else {
                        self.setLastHurtMob(victim)
                    }
                    super.onAttack(self, victim)
                    return EventResult.CONSUME
                }
            }
        )
}