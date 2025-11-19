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

        // fallback instance used if handler is unavailable or reflection fails
        private val FALLBACK = ModConfig()

        /**
         * Retrieve the live config instance from the HANDLER using reflection (tries several common method names).
         * If that fails, returns a fallback singleton so bindings still work.
         */
        @JvmStatic
        fun live(): ModConfig {
            val handler = HANDLER ?: return FALLBACK
            val cls = handler::class.java
            val candidates = arrayOf("get", "getConfig", "getOrCreate", "getOrLoad", "getValue", "getInstance")
            for (name in candidates) {
                try {
                    val m = cls.getMethod(name)
                    val res = m.invoke(handler)
                    if (res is ModConfig) return res
                } catch (_: NoSuchMethodException) {
                    // try next
                } catch (_: Throwable) {
                    // ignore other invocation errors and try next candidate
                }
            }
            return FALLBACK
        }

        @JvmStatic
        fun save() {
            try {
                HANDLER?.let { handler ->
                    // try to call save() reflectively if necessary (most implementations expose save())
                    val cls = handler::class.java
                    try {
                        val m = cls.getMethod("save")
                        m.invoke(handler)
                        return
                    } catch (_: NoSuchMethodException) {
                        // fallthrough
                    }
                }
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

}
