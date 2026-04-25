pluginManagement {
	val loom_version: String by settings
	plugins {
		id("net.fabricmc.fabric-loom") version loom_version
	}
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/snapshots")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9.1-beta.2"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	shared {
		versions("26.1")
    }
	create(rootProject)

}

rootProject.name = "YoinkGUI"