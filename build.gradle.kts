import me.modmuss50.mpp.ReleaseType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

val modVersion = "2.0.1"
val releaseType: ReleaseType = ReleaseType.BETA

val mcVersion = property("deps.minecraft_version").toString()
val mcDep = property("mcDep").toString()
val YACL = property("deps.yacl").toString()
val modMenu = property("deps.modmenu").toString()

version = "${modVersion}+${property("mod.mod_version")}"
group = property("maven_group") as String

base {
    archivesName.set(property("mod.archives_base_name") as String)
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
    maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
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
    minecraft("com.mojang:minecraft:${property("deps.minecraft_version")}")

    //Fabric
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    //Libraries
    implementation(include("net.kyori:adventure-platform-fabric:${property("deps.adventure_api")}")!!)

    //Config
    implementation("com.terraformersmc:modmenu:$modMenu")
    implementation("dev.isxander:yet-another-config-lib:$YACL")

    //Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //Dev
    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mc" to mcDep,
        "yaclVersion" to YACL,
        "modmenuVersion" to modMenu
    )
    props.forEach(inputs::property)
    filesMatching("fabric.mod.json") { expand(props) }
}

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to modVersion,
        "mcVersion" to mcVersion,
        "releaseType" to releaseType.toString()
    )
    inputs.properties(props)
    from(file("../../src/templates/kotlin"))
    into(layout.buildDirectory.dir("generated/sources/templates"))
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
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
            .takeIf { it.exists() }?.readText()
            ?: "No changelog provided."
    )

    type = STABLE
    modLoaders.add("fabric")

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')?.map { it.trim() } ?: emptyList()

    val slugs: List<String> = listOf(
        "fabric-api",
        "fabric-language-kotlin",
        "yacl",
        "modmenu"
    )

    modrinth {
        type.set(releaseType)
        projectId.set(property("modrinthId") as String)
        accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
        minecraftVersions.addAll(versionList("pub.modrinthMC"))
        slugs.forEach { s ->
            requires { slug.set(s) }
        }
    }

    curseforge {
        type.set(releaseType)
        projectId.set(property("curseforgeId") as String)
        accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
        minecraftVersions.addAll(versionList("pub.curseMC"))
        slugs.forEach { s ->
            requires { slug.set(s) }
        }
    }
}
