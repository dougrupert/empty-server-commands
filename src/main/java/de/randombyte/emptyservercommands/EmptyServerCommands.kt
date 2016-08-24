package de.randombyte.emptyservercommands

import com.google.inject.Inject
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit

@Plugin(id = EmptyServerCommands.ID, name = EmptyServerCommands.NAME, version = EmptyServerCommands.VERSION,
        authors = arrayOf(EmptyServerCommands.AUTHOR))
class EmptyServerCommands @Inject constructor(val logger: Logger,
                                              @DefaultConfig(sharedRoot = true) val configLoader: ConfigurationLoader<CommentedConfigurationNode>) {
    companion object {
        const val ID = "empty-server-commands"
        const val NAME = "EmptyServerCommands"
        const val VERSION = "v0.1"
        const val AUTHOR = "RandomByte"
    }

    var commandsExecutionTask: Task? = null // waiting for execution(not cancelled) when the server is empty

    @Listener
    fun onInit(event: GameInitializationEvent) {
        if (!Config.get(configLoader).waitForFirstPlayer) startCountdown() // Also generates config
        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onPlayerJoin(event: ClientConnectionEvent.Join) { commandsExecutionTask?.cancel() }

    @Listener
    fun onPlayerLeave(event: ClientConnectionEvent.Disconnect) {
        if (Sponge.getServer().onlinePlayers.size <= 1) startCountdown() // player is removed from onlinePlayer after this event
    }

    private fun startCountdown() {
        commandsExecutionTask = Task.builder()
                .delay(Config.get(configLoader).delay.toLong(), TimeUnit.MINUTES)
                .execute { ->
                    logger.info("No players on the server for a while => Executing commands!")
                    Config.get(configLoader).commands.forEach { Sponge.getCommandManager().process(Sponge.getServer().console, it) }
                }.submit(this)
    }
}