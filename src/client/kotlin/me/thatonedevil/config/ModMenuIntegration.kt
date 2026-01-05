package me.thatonedevil.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.config.YaclConfigHelper.booleanOption
import me.thatonedevil.config.YaclConfigHelper.enumOptionString
import me.thatonedevil.nbt.ComponentValueRegistry.refreshHandlers
import me.thatonedevil.nbt.FormatOptions
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parentScreen ->
        createScreen(parentScreen)
    }

    fun createScreen(parentScreen: Screen?): Screen {
        val screen = YetAnotherConfigLib.createBuilder()
            .save {
                YoinkGuiSettings.saveToFile()
                refreshHandlers()
            }
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
                    .option(booleanOption(
                        name = "Enable Single Item Yoink",
                        field = yoinkGuiSettings.enableSingleItemYoink,
                        defaultValue = true,
                        description = "Allows yoinking single items when pressing X while hovering."
                    ))
                    .build())
                .build())

            .category(ConfigCategory.createBuilder()
                .name(Text.of("Dev settings"))
                .tooltip(Text.of("Developer settings"))
                .group(OptionGroup.createBuilder()
                    .name(Text.of("Dev Options"))
                    .option(booleanOption(
                        name = "Debug Mode",
                        field = yoinkGuiSettings.debugMode,
                        defaultValue = false,
                        description = "Enables debug logging to help diagnose issues."
                    ))
                    .name(Text.of("Toggle formatting option"))
                    .option(enumOptionString(
                        name = "Default NBT Format",
                        field = yoinkGuiSettings.formatOption,
                        enumClass = FormatOptions::class.java,
                        defaultValue = FormatOptions.LEGACY
                    ))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.of("Nbt Parser Options"))
                    .option(booleanOption(
                        name = "Include Raw NBT",
                        field = yoinkGuiSettings.includeRawNbt,
                        defaultValue = false,
                        description = "Includes the raw NBT data in the parsed output."
                    ))
                    .option(booleanOption(
                        name = "Color parser",
                        field = yoinkGuiSettings.toggleColorParser,
                        defaultValue = true,
                        description = "Toggles color parsing in NBT text. <red>, <blue>, <green>"
                    ))
                    .option(booleanOption(
                        name = "Style parser",
                        field = yoinkGuiSettings.toggleStyleParser,
                        defaultValue = true,
                        description = "Toggles style parsing in NBT text. <bold>, <italic>, <underlined>"
                    ))
                    .option(booleanOption(
                        name = "Shadow parser",
                        field = yoinkGuiSettings.toggleShadowParser,
                        defaultValue = true,
                        description = "Toggles shadow parsing in NBT text. <shadow:#000000:0.5>"
                    ))
                    .option(booleanOption(
                        name = "Gradient parser",
                        field = yoinkGuiSettings.toggleGradientParser,
                        defaultValue = true,
                        description = "Toggles gradient parsing in NBT text. <gradient:#FF0000:#00FF00:#0000FF>"
                    ))
                    .build())
                .build())

            .build()
            .generateScreen(parentScreen)
        return screen
    }

}
