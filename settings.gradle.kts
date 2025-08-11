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
	id("dev.kikugie.stonecutter") version "0.7-alpha.22"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	shared {
		versions("1.21.8")
	}
	create(rootProject)

}