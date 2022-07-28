plugins {
    kotlin("jvm") version "1.6.10"

    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

group = "org.babyfish.jimmer.example.kt"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.babyfish.jimmer:jimmer-core-kotlin:0.1.1")

    ksp("org.babyfish.jimmer:jimmer-ksp:0.1.1")
}

// Without this configuration, gradle command can still run.
// However, Intellij cannot find the generated source.
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
