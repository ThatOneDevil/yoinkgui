plugins {
	id("org.jetbrains.kotlin.jvm") version "2.2.0"
	id("fabric-loom") version "1.13-SNAPSHOT"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

val modVersion = "1.6.2"

version = "${modVersion}+${property("mod.mod_version") as String}"
group = property("maven_group") as String

var cleanVersion = version.toString().split("+").first()
val mcVersion = property("deps.minecraft_version")!!.toString()
val mcDep = property("mcDep").toString()
val yacl = property("deps.yacl").toString()
val modMenu = property("deps.modmenu").toString()

base {
	archivesName.set(property("mod.archives_base_name") as String)
}

repositories {
	mavenCentral()
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots/"){
		name = "sonatype-oss-snapshots1"
		mavenContent { snapshotsOnly() }
	}
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }

    maven( "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        name = "DevAuth"
    }
}

loom {
	splitEnvironmentSourceSets()


	mods {
		create("yoinkgui").project.sourceSets {
            sourceSets["main"]
            sourceSets["client"]
        }
	}

	runConfigs.all {
		ideConfigGenerated(true)
		runDir("../../run")
	}

}

dependencies {
	// mappings
    minecraft("com.mojang:minecraft:${property("deps.minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")

    // fabric
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // adventure
	modImplementation(include("net.kyori:adventure-platform-fabric:${property("deps.adventure_api")}")!!)


    // config libs
    modImplementation("com.terraformersmc:modmenu:${modMenu}")
    modImplementation("dev.isxander:yet-another-config-lib:${yacl}")

    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //devauth
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

}

tasks.processResources {
	val mcDep: String by project

	val props = mapOf(
		"version" to project.version,
		"mc" to mcDep,
        "yaclVersion" to yacl,
        "modmenuVersion" to modMenu
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
        requires { slug.set("yacl") }
        requires { slug.set("modmenu") }
	}

	curseforge {
		projectId.set(property("curseforgeId") as String)
		accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
		minecraftVersions.addAll(versionList("pub.curseMC"))

		requires { slug.set("fabric-api") }
		requires { slug.set("fabric-language-kotlin") }
        requires { slug.set("yacl") }
        requires { slug.set("modmenu") }
	}

}


