package me.thatonedevil.nbt

import me.thatonedevil.BuildConfig
import java.io.BufferedWriter

internal object NBTFileWriter {
    fun writeHeader(
        writer: BufferedWriter,
        formattedTime: String,
        itemCount: Int,
        totalCount: Int
    ) {
        writer.write("=== Formatted NBT Data ===\n")
        writer.write("Generated: $formattedTime\n")
        writer.write("Items with content: $itemCount / $totalCount\n\n")

        writer.write("=== Details ===\n")
        writer.write("Mod Version: ${BuildConfig.VERSION}\n")
        writer.write("Minecraft Version: ${BuildConfig.MC_VERSION}\n\n")
    }

    fun writeItem(
        writer: BufferedWriter,
        index: Int,
        item: NBTParser.ParsedItem,
        includeRawNbt: Boolean,
        isLastItem: Boolean
    ) {
        writer.write("=== ITEM ${index + 1} ===\n")

        if (includeRawNbt) {
            writer.write("Raw NBT: ${item.raw}\n")
        }

        writer.write("\n${item.formatted}\n")

        if (!isLastItem) {
            writer.write("\n${"=".repeat(50)}\n\n")
        }
    }
}