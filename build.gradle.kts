plugins {
    id("fabric-loom")
    kotlin("jvm").version(System.getProperty("kotlin_version"))
    kotlin("kapt").version(System.getProperty("kotlin_version"))
}

base {
    archivesName.set(project.extra["archives_base_name"] as String)
}

version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

repositories {
    maven("https://maven.wispforest.io")
}

dependencies {
    minecraft("com.mojang", "minecraft", project.extra["minecraft_version"] as String)
    mappings("net.fabricmc", "yarn", project.extra["yarn_mappings"] as String, null, "v2")
    kapt("io.wispforest", "owo-lib", project.extra["owo_version"] as String)
    modImplementation("net.fabricmc", "fabric-loader", project.extra["loader_version"] as String)
    modImplementation("net.fabricmc.fabric-api", "fabric-api", project.extra["fabric_version"] as String)
    modImplementation(
        "net.fabricmc",
        "fabric-language-kotlin",
        project.extra["fabric_language_kotlin_version"] as String
    )
    modImplementation("io.wispforest", "owo-lib", project.extra["owo_version"] as String)
    implementation("io.ktor", "ktor-server-core-jvm", project.extra["ktor_version"] as String)
    implementation("io.ktor", "ktor-server-netty-jvm", project.extra["ktor_version"] as String)
    implementation("io.ktor", "ktor-server-content-negotiation", project.extra["ktor_version"] as String)
    implementation("io.ktor", "ktor-serialization-gson", project.extra["ktor_version"] as String)
    implementation("io.ktor", "ktor-server-auth", project.extra["ktor_version"] as String)
    include("io.wispforest", "owo-sentinel", project.extra["owo_version"] as String)
}

tasks {
    val javaVersion = JavaVersion.toVersion((project.extra["java_version"] as String).toInt())

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.extra["mod_version"] as String,
                    "fabricloader" to project.extra["loader_version"] as String,
                    "fabric_api" to project.extra["fabric_version"] as String,
                    "fabric_language_kotlin" to project.extra["fabric_language_kotlin_version"] as String,
                    "minecraft" to project.extra["minecraft_version"] as String,
                    "java" to project.extra["java_version"] as String
                )
            )
        }
        filesMatching("*.mixins.json") {
            expand(
                mutableMapOf(
                    "java" to project.extra["java_version"] as String
                )
            )
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    exec {
        if (System.getenv("GITHUB_OUTPUT") != null) {
            commandLine(
                "sh",
                "-c",
                "echo MOD_VERSION=${project.extra["mod_version"]} >> ${System.getenv("GITHUB_OUTPUT")}"
            )
        } else {
            commandLine("echo", "MOD_VERSION=${project.extra["mod_version"]}")
        }
    }
    exec {
        if (System.getenv("GITHUB_OUTPUT") != null) {
            commandLine(
                "sh",
                "-c",
                "echo MINECRAFT_VERSION=${project.extra["minecraft_version"]} >> ${System.getenv("GITHUB_OUTPUT")}"
            )
        } else {
            commandLine("echo", "MINECRAFT_VERSION=${project.extra["minecraft_version"]}")
        }
    }
    exec {
        if (System.getenv("GITHUB_OUTPUT") != null) {
            commandLine(
                "sh",
                "-c",
                "echo JAVA_VERSION=${project.extra["java_version"]} >> ${System.getenv("GITHUB_OUTPUT")}"
            )
        } else {
            commandLine("echo", "JAVA_VERSION=${project.extra["java_version"]}")
        }
    }
}
