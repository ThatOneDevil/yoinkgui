package me.thatonedevil.config

import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.EntryAddable
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import net.fabricmc.loader.api.FabricLoader

open class YoinkGuiSettings() : JsonFileCodecConfig<YoinkGuiSettings>(
    FabricLoader.getInstance().configDir.resolve("yoinkgui.json")
) {
    constructor(settings: YoinkGuiSettings) : this() {
        this.enableYoinkButton.value = settings.enableYoinkButton.value
        this.buttonScaleFactor.value = settings.buttonScaleFactor.value
        this.debugMode.value = settings.debugMode.value
        this._firstLaunch.value = settings._firstLaunch.value
        this.serializeItemstack.value = settings.serializeItemstack.value
    }

    val enableYoinkButton by register<Boolean>(default = true, BOOL)
    val buttonScaleFactor by register<Float>(default = 1.0f, FLOAT)
    val debugMode by register<Boolean>(default = false, BOOL)
    val serializeItemstack by register<Boolean>(default = false, BOOL)

    var firstLaunch = false
    val _firstLaunch by register<Boolean>(default = true, BOOL)

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
