package me.thatonedevil.config

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.value
import net.minecraft.text.Text

object YaclConfigHelper {

    fun booleanOption(
        name: String,
        field: ConfigEntry<Boolean>,
        defaultValue: Boolean = true,
        description: String? = null
    ): Option<Boolean> {
        return Option.createBuilder<Boolean>()
            .name(Text.of(name))
            .apply { description?.let { description(OptionDescription.of(Text.of(it))) } }
            .binding(defaultValue, { field.value }, { field.value = it })
            .controller(TickBoxControllerBuilder::create)
            .build()
    }

    fun floatSliderOption(
        name: String,
        field: ConfigEntry<Float>,
        defaultValue: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float = 0.1f,
        formatValue: ((Float) -> Text)? = null,
        description: String? = null
    ): Option<Float> {
        return Option.createBuilder<Float>()
            .name(Text.of(name))
            .apply { description?.let { description(OptionDescription.of(Text.of(it))) } }
            .binding(defaultValue, { field.value }, { field.value = it })
            .controller { option ->
                FloatSliderControllerBuilder.create(option)
                    .range(range.start, range.endInclusive)
                    .step(step)
                    .apply { formatValue?.let { formatValue(it) } }
            }
            .build()
    }
}

