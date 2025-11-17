plugins {
	id("org.jetbrains.kotlin.jvm") version "2.2.0"
	id("fabric-loom") version "1.11-SNAPSHOT"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

version = property("mod.mod_version") as String
group = property("maven_group") as String
var cleanVersion = version.toString().split("+").first()
val mcVersion = property("deps.minecraft_version")!!.toString()
val mcDep = property("mcDep").toString()

base {
	archivesName.set(property("mod.archives_base_name") as String)
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

	runConfigs.all {
		ideConfigGenerated(true)
		runDir("../../run")
	}

}

dependencies {
	minecraft("com.mojang:minecraft:${property("deps.minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
	modImplementation(include("net.kyori:adventure-platform-fabric:${property("deps.adventure_api")}")!!)

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.processResources {
	val mcDep: String by project

	val props = mapOf(
		"version" to project.version,
		"mc" to mcDep
	)

	props.forEach(inputs::property)

	filesMatching("fabric.mod.json") {
		expand(props)
	}

}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
}

java {
	withSourcesJar()
	val java = JavaVersion.VERSION_21
	targetCompatibility = java
	sourceCompatibility = java
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)
	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

val templateSource = file("../../src/templates/kotlin")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
	val props = mapOf("version" to cleanVersion, "mcVersion" to mcVersion)
	inputs.properties(props)

	from(templateSource)
	into(templateDest)
	expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

publishMods {
	displayName.set("YoinkGUI $cleanVersion for MC $mcVersion")
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(
		rootProject.file("changelogs/${cleanVersion}.md")
			.takeIf { it.exists() }
			?.readText()
			?: "No changelog provided."
	)
	type = STABLE
	modLoaders.add("fabric")

	fun versionList(prop: String) = findProperty(prop)?.toString()
		?.split(',')
		?.map { it.trim() }
		?: emptyList()

	modrinth {
		projectId.set(property("modrinthId") as String)
		accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
		minecraftVersions.addAll(versionList("pub.modrinthMC"))

		requires { slug.set("fabric-api") }
		requires { slug.set("fabric-language-kotlin") }
	}

	curseforge {
		projectId.set(property("curseforgeId") as String)
		accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
		minecraftVersions.addAll(versionList("pub.curseMC"))

		requires { slug.set("fabric-api") }
		requires { slug.set("fabric-language-kotlin") }
	}

}


