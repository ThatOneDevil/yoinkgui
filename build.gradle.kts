import me.modmuss50.mpp.ReleaseType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
	id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT"
	id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

val modVersion = "1.9.1"
val releaseType = ReleaseType.BETA

version = "${modVersion}+${property("mod.mod_version") as String}"
group = property("maven_group") as String

val mcVersion = property("deps.minecraft_version").toString()
val mcDep = property("mcDep").toString()
val yacl = property("deps.yacl").toString()
val modMenu = property("deps.modmenu").toString()

base {
	archivesName.set(property("mod.archives_base_name") as String)
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/"){
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
	mavenCentral()
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
    minecraft("com.mojang:minecraft:${project.property("deps.minecraft_version")}")
    mappings(loom.officialMojangMappings())

    // fabric
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // adventure
    implementation(include("net.kyori:adventure-platform-fabric:${property("deps.adventure_api")}")!!)


    // config libs
    implementation("com.terraformersmc:modmenu:${modMenu}")
    implementation("dev.isxander:yet-another-config-lib:${yacl}")

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

val templateSource = file("../../src/templates/kotlin")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to modVersion,
        "mcVersion" to mcVersion,
        "releaseType" to releaseType.toString())

    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
	withSourcesJar()
	val java = JavaVersion.VERSION_25
	targetCompatibility = java
	sourceCompatibility = java
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)
	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}


publishMods {
    displayName.set("YoinkGUI $modVersion for MC $mcVersion")
    file.set(tasks.jar.get().archiveFile)
    changelog.set(
        rootProject.file("src/main/resources/changelogs/${modVersion}.md")
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
        type.set(releaseType)
        projectId.set(property("modrinthId") as String)
        accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
        minecraftVersions.addAll(versionList("pub.modrinthMC"))

        requires { slug.set("fabric-api") }
        requires { slug.set("fabric-language-kotlin") }
        requires { slug.set("yacl") }
        requires { slug.set("modmenu") }
    }

    curseforge {
        type.set(releaseType)
        projectId.set(property("curseforgeId") as String)
        accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
        minecraftVersions.addAll(versionList("pub.curseMC"))

        requires { slug.set("fabric-api") }
        requires { slug.set("fabric-language-kotlin") }
        requires { slug.set("yacl") }
        requires { slug.set("modmenu") }
    }
}





