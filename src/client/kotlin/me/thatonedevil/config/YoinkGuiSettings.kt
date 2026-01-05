package me.thatonedevil.config

import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import me.thatonedevil.nbt.FormatOptions
import net.fabricmc.loader.api.FabricLoader

open class YoinkGuiSettings() : JsonFileCodecConfig<YoinkGuiSettings>(
    FabricLoader.getInstance().configDir.resolve("yoinkgui.json")
) {
    constructor(settings: YoinkGuiSettings) : this() {
        this.enableYoinkButton.value = settings.enableYoinkButton.value
        this.buttonScaleFactor.value = settings.buttonScaleFactor.value
        this.buttonX.value = settings.buttonX.value
        this.buttonY.value = settings.buttonY.value
        this.debugMode.value = settings.debugMode.value
        this._firstLaunch.value = settings._firstLaunch.value
    }

    val enableYoinkButton by register<Boolean>(default = true, BOOL)
    val enableSingleItemYoink by register<Boolean>(default = true, BOOL)

    val buttonScaleFactor by register<Float>(default = 1.0f, FLOAT)
    val buttonX by register<Int>(default = 40, INT)
    val buttonY by register<Int>(default = 35, INT)
    val debugMode by register<Boolean>(default = false, BOOL)

    // parser options
    val formatOption by register<String>(default = FormatOptions.LEGACY.name, STRING)
    val includeRawNbt by register<Boolean>(default = false, BOOL)
    val toggleColorParser by register<Boolean>(default = true, BOOL)
    val toggleStyleParser by register<Boolean>(default = true, BOOL)
    val toggleShadowParser by register<Boolean>(default = true, BOOL)
    val toggleGradientParser by register<Boolean>(default = true, BOOL)

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
