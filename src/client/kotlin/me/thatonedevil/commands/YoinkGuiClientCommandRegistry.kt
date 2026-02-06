package me.thatonedevil.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import me.thatonedevil.config.ModMenuIntegration
import me.thatonedevil.screen.ButtonPositionScreen
import me.thatonedevil.screen.changelog.ChangelogScreen
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickURL
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.kyori.adventure.text.Component

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
                .addCommand("version") {
                    UpdateChecker.checkVersion()
                }
                .addCommand("menu") {
                    it.source.client.send {
                        it.source.client.setScreen(ButtonPositionScreen(it.source.client.currentScreen))
                    }
                }
                .addCommand("changelog") {
                    it.source.client.send {
                        it.source.client.setScreen(ChangelogScreen(it.source.client.currentScreen))
                    }
                }
                .addCommand("debug") {
                    debugCommand.execute()
                }
                .addCommand("config") {
                    it.source.client.send {
                        it.source.client.setScreen(ModMenuIntegration().createScreen(null))
                    }
                }
                .addCommand("discord") {
                    sendChat(
                        Component.text()
                            .append("<color:#5865F2>https://discord.com/invite/kcegGvZvpC &7&o(Click to open)".toClickURL("https://discord.com/invite/kcegGvZvpC"))
                            .build()
                    )
                }
        )
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.addCommand(
        name: String,
        action: (CommandContext<FabricClientCommandSource>) -> Unit
    ) = this.then(
        ClientCommandManager.literal(name)
            .executes {
                action(it)
                Command.SINGLE_SUCCESS
            }
    )
}
