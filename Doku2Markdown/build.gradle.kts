import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
}

group = "Fudge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("it.skrape:skrapeit-core:0.5.1")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0")
    compile("com.vladsch.flexmark:flexmark-all:0.50.40")
    implementation ("junit:junit:4.13-beta-3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}