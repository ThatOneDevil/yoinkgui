package me.thatonedevil.config

import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import net.fabricmc.loader.api.FabricLoader

open class YoinkGuiSettings() : JsonFileCodecConfig<YoinkGuiSettings>(
    FabricLoader.getInstance().configDir.resolve("yoinkgui.json")
) {
    val enableYoinkButton by register<Boolean>(default = true, BOOL)
    val buttonScaleFactor by register<Float>(default = 1.0f, FLOAT)

    var firstLaunch = false
    val _firstLaunch by register<Boolean>(default = true, BOOL)

    constructor(settings: YoinkGuiSettings) : this() {
        this.enableYoinkButton.value = settings.enableYoinkButton.value
    }

    companion object : YoinkGuiSettings() {
        init {
            if (!loadFromFile()) {
                saveToFile()
            }

            if (_firstLaunch.value) {
                firstLaunch = true
                _firstLaunch.value = false
                saveToFile()
            }

        }
    }
}
