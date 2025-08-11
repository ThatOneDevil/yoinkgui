plugins {
	id("fabric-loom") version "1.11-SNAPSHOT"
	id("maven-publish")
	id("org.jetbrains.kotlin.jvm") version "2.2.0"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}



version = property("mod_version") as String
group = property("maven_group") as String

base {
	archivesName.set(property("archives_base_name") as String)
}

repositories {
	mavenCentral()
	maven {
		name = "sonatype-oss-snapshots1"
		url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
		mavenContent { snapshotsOnly() }
	}
}


loom {
	splitEnvironmentSourceSets()

	mods {
		create("yoinkgui") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

	modImplementation(include("net.kyori:adventure-platform-fabric:6.5.1")!!)
	implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-api:2.22.0")
	implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-core:2.22.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.processResources {
	inputs.property("version", project.version)
	filesMatching("fabric.mod.json") {
		expand("version" to inputs.properties["version"])
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)
	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

val generatedDir = layout.buildDirectory.dir("generated/sources/buildConfig").get().asFile

sourceSets["main"].java.srcDir(generatedDir)

val generateBuildConfig by tasks.registering(Copy::class) {
	from("src/templates/kotlin")
	into(generatedDir)
	filteringCharset = "UTF-8"
	expand("version" to project.version)
	inputs.property("version", project.version)
	outputs.upToDateWhen { false }
}

tasks.named("compileKotlin") {
	dependsOn(generateBuildConfig)
}
tasks.named("sourcesJar") {
	dependsOn(generateBuildConfig)
}


publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(
		rootProject.file("changelogs/${version}.md")
			.takeIf { it.exists() }
			?.readText()
			?: "No changelog provided."
	)
	type = STABLE
	modLoaders.add("fabric")


	modrinth {
		projectId.set(property("modrinthId") as String)
		accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
		minecraftVersions.addAll("1.21.8")

		requires { slug.set("fabric-api") }
		requires { slug.set("fabric-language-kotlin") }
	}

	curseforge {
		projectId.set(property("curseforgeId") as String)
		accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
		minecraftVersions.addAll("1.21.8")

		requires { slug.set("fabric-api") }
		requires { slug.set("fabric-language-kotlin") }
	}

}
