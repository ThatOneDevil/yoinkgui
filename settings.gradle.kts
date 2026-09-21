pluginManagement {
	val loom_version = providers.gradleProperty("loom_version").get()
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
	id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	shared {
		versions("26.3")
    }
	create(rootProject)

}

rootProject.name = "YoinkGUI"