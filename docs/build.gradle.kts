val ORCHID_VERSION: String by project

// 1. Apply Orchid plugin
plugins {
    id("com.eden.orchidPlugin")
    id("kotlinx-knit")
}

fun DependencyHandlerScope.orchidImplementation(name: String, version: String = ORCHID_VERSION) =
    orchidImplementation("io.github.javaeden.orchid", name, version)

fun DependencyHandlerScope.orchidRuntimeOnly(name: String, version: String = ORCHID_VERSION) =
    orchidRuntimeOnly("io.github.javaeden.orchid", name, version)

val compilerPlugin by configurations.creating

// 2. Include Orchid dependencies
dependencies {
    orchidImplementation("OrchidCore")
    orchidImplementation("OrchidCopper")

    orchidRuntimeOnly("OrchidDocs")
    orchidRuntimeOnly("OrchidKotlindoc")
    orchidRuntimeOnly("OrchidPluginDocs")
    orchidRuntimeOnly("OrchidGithub")
    orchidRuntimeOnly("OrchidChangelog")
    orchidRuntimeOnly("OrchidSyntaxHighlighter")
    orchidRuntimeOnly("OrchidSnippets")
    orchidRuntimeOnly("OrchidCopper")
    orchidRuntimeOnly("OrchidWiki")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("io.mockk", "mockk", "1.10.2")

    // generated test dependencies
    val KNIT_VERSION: String by project
    val ARROW_VERSION: String by project
    val COROUTINES_VERSION: String by project
    testImplementation("org.jetbrains.kotlinx:kotlinx-knit-test:$KNIT_VERSION")
    testImplementation(kotlin("stdlib"))
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", COROUTINES_VERSION)
    testCompileOnly("io.arrow-kt", "arrow-annotations", ARROW_VERSION)
    testImplementation(project(":hooks"))
    compilerPlugin(project(":compiler-plugin"))
}

// 4. Use the 'Editorial' theme, and set the URL it will have on Github Pages
orchid {
    githubToken = System.getenv("GITHUB_TOKEN")
}

val compileOrchidKotlin by tasks.getting(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

val generatedSourcesRoot: String = buildDir.absolutePath

sourceSets {
    test {
        java {
            srcDir("$generatedSourcesRoot/generated/source/kapt/main")
        }
    }
}

tasks {
    val cleanGenerated by registering {
        group = "build"
        delete(generatedSourcesRoot)
    }

    compileTestKotlin {
        dependsOn(cleanGenerated)
        dependsOn(compilerPlugin)
        kotlinOptions {
            verbose = true
            freeCompilerArgs = listOf(
                "-Xplugin=${compilerPlugin.resolve().first()}"
            )
        }
    }
}
