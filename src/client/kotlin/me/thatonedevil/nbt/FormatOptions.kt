package me.thatonedevil.nbt

import dev.isxander.yacl3.api.NameableEnum
import net.minecraft.network.chat.Component

enum class FormatOptions : NameableEnum {

    MINIMESSAGE,
    LEGACY;

    override fun getDisplayName(): Component? {
        return when (this) {
            MINIMESSAGE -> Component.nullToEmpty("MINIMESSAGE")
            LEGACY -> Component.nullToEmpty("LEGACY")
        }
    }
}