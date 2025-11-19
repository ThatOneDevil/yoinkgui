package me.thatonedevil.config

import com.google.gson.GsonBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier


class ModConfig() {

    @JvmField @SerialEntry
    var enableYoinkButton: Boolean = true

    @JvmField @SerialEntry
    var buttonScaleFactor: Float = 1.0f

    companion object {
        @JvmField
        val HANDLER: ConfigClassHandler<ModConfig>? = ConfigClassHandler.createBuilder(ModConfig::class.java)
            .id(Identifier.of("config"))
            .serializer { config ->
                GsonConfigSerializerBuilder.create<ModConfig>(config)
                    .setPath(FabricLoader.getInstance().configDir.resolve("yoinkGUI_config.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
                    .setJson5(true)
                    .build()
            }
            .build()
    }

}
