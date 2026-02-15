package me.thatonedevil.config

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.value
import net.minecraft.network.chat.Component

object YaclConfigHelper {

    fun booleanOption(
        name: String,
        field: ConfigEntry<Boolean>,
        defaultValue: Boolean = true,
        description: String? = null
    ): Option<Boolean> {

        return Option.createBuilder<Boolean>()
            .name(Component.nullToEmpty(name))
            .apply { description?.let { description(OptionDescription.of(Component.nullToEmpty(it))) } }
            .binding(defaultValue, { field.value }, { field.value = it })
            .controller(TickBoxControllerBuilder::create)
            .build()
    }


    fun <T : Enum<T>> enumOptionString(
        name: String,
        field: ConfigEntry<String>,
        enumClass: Class<T>,
        defaultValue: T
    ): Option<T> {
        return Option.createBuilder<T>()
            .name(Component.nullToEmpty(name))
            .binding(
                defaultValue,
                { try { java.lang.Enum.valueOf(enumClass, field.value) } catch (_: Exception) { defaultValue } },
                { field.value = it.name }
            )
            .controller { option -> EnumControllerBuilder.create(option).enumClass(enumClass) }
            .build()
    }

}

