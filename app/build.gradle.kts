plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    kotlin("plugin.serialization") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Browser automation
    implementation("com.microsoft.playwright:playwright:1.57.0")

    // Google Sheets API
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20251110-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.36.0")
    implementation("com.google.http-client:google-http-client-gson:1.45.0")

    // Configuration
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // CLI
    implementation("com.github.ajalt.clikt:clikt:5.0.2")

    // Testing
    testImplementation("io.mockk:mockk:1.13.9")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("2.2.0")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.example.cli.MainKt"
}
