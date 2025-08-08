package me.thatonedevil

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object YoinkGUI : ModInitializer {
	const val MOD_ID = "yoinkGUI"
	val logger = LoggerFactory.getLogger(MOD_ID)
	override fun onInitialize() {
		logger.info("Hello Fabric world!")

	}
}