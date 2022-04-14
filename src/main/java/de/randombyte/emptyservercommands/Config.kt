package de.randombyte.emptyservercommands

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class Config {
    companion object {
        private val typeToken = TypeToken.of(Config::class.java)

        fun get(configLoader: ConfigurationLoader<CommentedConfigurationNode>): Config = configLoader.load().getValue(typeToken) ?: {
            save(configLoader, Config())
            get(configLoader)
        }.invoke()

        fun save(configLoader: ConfigurationLoader<CommentedConfigurationNode>, config: Config) = configLoader.apply {
            save(load().setValue(typeToken, config))
        }
    }
    @Setting(comment = "Duration in minutes the server has to be empty to execute the commands.") val delay: Int = 20
    @Setting(value = "wait-for-first-player",
            comment = "Whether it should be waited for at least one player to join before the plugin is activated.")
        val waitForFirstPlayer: Boolean = false
    @Setting val emptyCommands: List<String> = listOf("say Server is empty")
    @Setting(value = "trigger-player-count",
            comment = "Max. amount of players that can be on the server and it is still marked as 'empty'.")
        val triggerPlayerCount: Int = 0
}