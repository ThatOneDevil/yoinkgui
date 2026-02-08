pluginManagement {
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
	id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	shared {
		versions("1.21.1-fabric", "1.21.4-fabric", "1.21.5-fabric", "1.21.8-fabric", "1.21.9-fabric")
	}
	create(rootProject)

}

rootProject.name = "YoinkGUI"