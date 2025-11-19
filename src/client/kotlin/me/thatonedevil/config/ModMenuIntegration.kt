package me.thatonedevil.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parentScreen ->
        YetAnotherConfigLib.createBuilder()
            .title(Text.of("YoinkGUI Settings"))
            .category(ConfigCategory.createBuilder()
                .name(Text.of("Button settings"))
                .tooltip(Text.of("Button settings"))
                .group(OptionGroup.createBuilder()
                    .name(Text.of("Button Options"))

                    .option(Option.createBuilder<Boolean>()
                        .name(Text.of("Enable Yoink Button"))
                        .binding(
                            ModConfig.live().enableYoinkButton,
                            { ModConfig.live().enableYoinkButton },
                            { ModConfig.live().enableYoinkButton = it }
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())

                    .option(Option.createBuilder<Float>()
                        .name(Text.of("Button Scale Factor"))
                        .binding(
                            ModConfig.live().buttonScaleFactor,
                            { ModConfig.live().buttonScaleFactor },
                            { ModConfig.live().buttonScaleFactor = it }
                        )
                        .controller{ option ->
                            FloatSliderControllerBuilder.create(option)
                                .range(0f, 2f)
                                .step(0.1f)
                                .formatValue { value ->
                                    Text.of(" ${value}x scale")
                                }
                        }
                        .build())
                    .build())
                .build())
            .save { ModConfig.save() }
            .build()
            .generateScreen(parentScreen)
    }

}