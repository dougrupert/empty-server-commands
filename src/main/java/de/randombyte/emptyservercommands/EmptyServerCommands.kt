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
        const val VERSION = "v0.2"
        const val AUTHOR = "RandomByte"
    }

    var commandsExecutionTask: Task? = null // waiting for execution(not cancelled) when the server is empty
    var emptyCommandsExecuted: Boolean = false

    fun tooFewPlayersOnline(onlinePlayersModifier: Int = 0) =
            Sponge.getServer().onlinePlayers.size + onlinePlayersModifier <= Config.get(configLoader).triggerPlayerCount

    @Listener
    fun onInit(event: GameInitializationEvent) {
        if (!Config.get(configLoader).waitForFirstPlayer && tooFewPlayersOnline()) startCountdown() // Also generates config
        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onPlayerJoin(event: ClientConnectionEvent.Join) {
        if (!tooFewPlayersOnline() && emptyCommandsExecuted) {
            logger.info("Player joined empty server, executing commands")
            Config.get(configLoader).commandsPlayers.forEach { Sponge.getCommandManager().process(Sponge.getServer().console, it) }
            emptyCommandsExecuted = false
        } else if (!tooFewPlayersOnline() && !emptyCommandsExecuted) {
            logger.info("Player cancelled empty commands")
            commandsExecutionTask?.cancel()
        }
    }

    @Listener
    fun onPlayerLeave(event: ClientConnectionEvent.Disconnect) {
        if (tooFewPlayersOnline(-1)) startCountdown() // player is removed from onlinePlayers after this event
    }

    private fun startCountdown() {
        commandsExecutionTask = Task.builder()
                .delay(Config.get(configLoader).delay.toLong(), TimeUnit.MINUTES)
                .execute { ->
                    logger.info("No players on the server for a while, executing commands")
                    emptyCommandsExecuted = true
                    Config.get(configLoader).commandsEmpty.forEach { Sponge.getCommandManager().process(Sponge.getServer().console, it) }
                }.submit(this)
    }
}