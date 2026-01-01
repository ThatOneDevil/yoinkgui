package me.thatonedevil.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import me.thatonedevil.gui.ButtonPositionScreen
import me.thatonedevil.gui.ChangelogScreen
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object YoinkGuiClientCommandRegistry {

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
                        .executes {
                            UpdateChecker.checkVersion()
                            Command.SINGLE_SUCCESS
                        }
                ).then(
                    ClientCommandManager.literal("menu")
                        .executes { context ->
                            val client = context.source.client
                            client.send {
                                client.setScreen(ButtonPositionScreen(client.currentScreen))
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    ClientCommandManager.literal("changelog")
                        .executes { context ->
                            val client = context.source.client
                            client.send {
                                client.setScreen(ChangelogScreen(client.currentScreen))
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    ClientCommandManager.literal("debug")
                        .executes { _ -> debugCommand.execute() }
                )

        )
    }
}