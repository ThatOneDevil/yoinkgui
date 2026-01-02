package me.thatonedevil.nbt

import dev.isxander.yacl3.api.NameableEnum
import net.minecraft.text.Text

enum class FormatOptions : NameableEnum {

    MINIMESSAGE,
    LEGACY;

    override fun getDisplayName(): Text? {
        return when (this) {
            MINIMESSAGE -> Text.of("Minimessage")
            LEGACY -> Text.of("Legacy")
        }
    }
}