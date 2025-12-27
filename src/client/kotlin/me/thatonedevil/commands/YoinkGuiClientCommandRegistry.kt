package me.thatonedevil.commands

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object YoinkGuiClientCommandRegistry {

    private val versionCommand = VersionCommand()
    private val debugCommand = DebugCommand()

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            registerCommands(dispatcher)
        }

    }

    private fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(
        ClientCommandManager.literal("yoinkguiclient")
            .then(
                ClientCommandManager.literal("version")
                    .executes { _ ->
                        versionCommand.execute()
                    }
            ).then(
            ClientCommandManager.literal("debug")
                .executes { _ ->
                    debugCommand.execute()
                }
        )
        )
    }

}