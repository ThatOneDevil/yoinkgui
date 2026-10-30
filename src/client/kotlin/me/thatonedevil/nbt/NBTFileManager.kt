package me.thatonedevil.nbt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.ErrorReporter
import me.thatonedevil.utils.api.UpdateChecker
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTFileManager {
    private val timeFormatter =
        DateTimeFormatter.ofPattern("MM-dd HH-mm-ss")

    suspend fun saveFormattedNBTToFile(
        nbtList: List<String>,
        configDir: String
    ): Result<File> {
        return saveNbtFile(
            "$configDir/yoinkgui",
            nbtList
        )
    }

    suspend fun saveSingleItem(
        rawNbt: String,
        configDir: String
    ): Result<File> {
        return saveNbtFile(
            "$configDir/yoinkgui/items",
            listOf(rawNbt)
        )
    }

    private suspend fun saveNbtFile(
        directory: String,
        rawItems: List<String>
    ): Result<File> = withContext(Dispatchers.IO) {
        val start = LocalDateTime.now()
        val formattedTime = start.format(timeFormatter)

        try {
            val outputDirectory =
                ensureDirectoryExists(directory)

            val file = File(
                outputDirectory,
                generateFilename(formattedTime)
            )

            val items = NBTParser.parseItems(rawItems)

            file.bufferedWriter().use { writer ->
                NBTFileWriter.writeHeader(
                    writer = writer,
                    formattedTime = formattedTime,
                    itemCount = items.size,
                    totalCount = rawItems.size
                )

                items.forEachIndexed { index, item ->
                    NBTFileWriter.writeItem(
                        writer = writer,
                        index = index,
                        item = item,
                        includeRawNbt =
                            YoinkGuiSettings.includeRawNbt.get(),
                        isLastItem = index == items.lastIndex
                    )
                }
            }

            val duration =
                Duration.between(
                    start,
                    LocalDateTime.now()
                ).toMillis()

            sendSuccessMessage(file, duration)

            Result.success(file)
        } catch (e: Exception) {
            ErrorReporter.report(
                e,
                "Error saving NBT file to $directory",
                "Error saving NBT file"
            )

            Result.failure(e)
        }
    }

    private fun ensureDirectoryExists(path: String): File {
        return File(path).apply {
            if (!exists() && !mkdirs()) {
                throw IOException(
                    "Failed to create directory: $absolutePath"
                )
            }
        }
    }

    private fun generateFilename(
        formattedTime: String
    ): String {
        return "${UpdateChecker.serverName}-$formattedTime.txt"
    }

    private fun sendSuccessMessage(file: File, duration: Long) {
        val pink = 0xFFA6CA
        val purple = 0x8968CD
        val openFile = ClickEvent.OpenFile(file.toPath().toAbsolutePath())

        val relativePath = file.relativeTo(File(".").canonicalFile).path

        val header = Component.literal("Formatted NBT data saved to:")
            .withColor(pink)

        val parseTime = Component.literal(" Parse time: ")
            .withColor(pink)
            .append(
                Component.literal("${duration}ms")
                    .withColor(purple)
            )

        val filePath = Component.literal("  ")
            .append(
                Component.literal(relativePath)
                    .withStyle(
                        Style.EMPTY
                            .withColor(purple)
                            .withUnderlined(true)
                            .withClickEvent(openFile)
                    )
            )
            .append(
                Component.literal(" (Click to open file)")
                    .withStyle(
                        Style.EMPTY
                            .withColor(0xAAAAAA)
                            .withItalic(true)
                            .withClickEvent(openFile)
                    )
            )

        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.let { player ->
                player.sendSystemMessage(header)
                player.sendSystemMessage(parseTime)
                player.sendSystemMessage(filePath)
            }
        }
    }

}