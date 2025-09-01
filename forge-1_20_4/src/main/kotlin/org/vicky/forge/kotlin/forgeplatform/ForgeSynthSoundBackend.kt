package org.vicky.forge.kotlin.forgeplatform


import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.PlatformPlugin

private fun PlatformPlayer.asServerPlayer(): ForgePlatformPlayer =
    PlatformPlugin.getPlayer(this.uniqueId()).get() as ForgePlatformPlayer