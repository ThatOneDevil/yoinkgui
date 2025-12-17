package me.thatonedevil.commands

import com.mojang.brigadier.Command
import me.thatonedevil.utils.api.UpdateChecker

class VersionCommand {

    fun execute(): Int {
        UpdateChecker.checkVersion()
        return Command.SINGLE_SUCCESS
    }

}

