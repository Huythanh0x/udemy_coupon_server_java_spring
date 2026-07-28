import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.spring) apply false
    alias(libs.plugins.kotlin.plugin.jpa) apply false
    alias(libs.plugins.ktlint.gradle) apply false
    alias(libs.plugins.detekt) apply false
    java
}

allprojects {
    group = "com.thanh0x.coursedeal"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<KtlintExtension> {
        version.set("1.0.1")
        // Snapshots today's formatting violations so only newly introduced ones fail the
        // build going forward (see ktlint-baseline.xml). Regenerate with `ktlintGenerateBaseline`.
        baseline.set(file("$projectDir/ktlint-baseline.xml"))
    }

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        // Each module gets its own baseline snapshotting today's violations, so only
        // newly introduced issues fail the build going forward (see detekt-baseline.xml).
        baseline = file("$projectDir/detekt-baseline.xml")
    }
}

// Simplified task - delegates to a shell script (much less boilerplate)
tasks.register<Exec>("bootRunLocal") {
    group = "application"
    description = "Runs both API service and Crawler service in parallel with local profile"
    commandLine("bash", "scripts/bootRunLocal.sh")
}
