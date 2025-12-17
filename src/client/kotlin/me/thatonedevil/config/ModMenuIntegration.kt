package me.thatonedevil.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.config.YaclConfigHelper.floatSliderOption
import me.thatonedevil.config.YaclConfigHelper.booleanOption
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parentScreen ->
        createScreen(parentScreen)
    }

    private fun createScreen(parentScreen: Screen?): Screen {
        val screen = YetAnotherConfigLib.createBuilder()
            .save(YoinkGuiSettings::saveToFile)
            .title(Text.of("YoinkGUI Settings"))
            .category(ConfigCategory.createBuilder()
                .name(Text.of("Button settings"))
                .tooltip(Text.of("Button settings"))
                .group(OptionGroup.createBuilder()
                    .name(Text.of("Button Options"))

                    .option(booleanOption(
                        name = "Enable Yoink Button",
                        field = yoinkGuiSettings.enableYoinkButton,
                        defaultValue = true
                    ))

                    .option(floatSliderOption(
                        name = "Button Scale Factor",
                        field = yoinkGuiSettings.buttonScaleFactor,
                        defaultValue = 1.0f,
                        range = 0.1f..2f,
                        step = 0.1f,
                        formatValue = { Text.of("${"%.2f".format(it)}x scale") }
                    ))
                    .build())
                .build())
            .build()
            .generateScreen(parentScreen)
        return screen
    }

}