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
    @Setting(comment = "Duration in minutes the server has to be empty to be shut down.") val delay: Int = 20
    @Setting(value = "wait-for-first-player",
            comment = "Whether it should be waited for at least one player to join before the plugin is activated.")
        val waitForFirstPlayer: Boolean = false
    @Setting val commands: List<String> = listOf("stop")
}