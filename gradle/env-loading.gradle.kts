// Shared by coupon-api-service and coupon-crawler-service: loads .env into the
// bootRun task's environment so `./gradlew bootRun` behaves like the Docker Compose
// setup without requiring engineers to export vars manually.
//
// Uses the core JavaExec type (BootRun's superclass) rather than importing
// org.springframework.boot.gradle.tasks.run.BootRun directly: a script applied via
// `apply(from = ...)` does not see the classpath contributed by the `plugins {}` block
// of the project that applies it, so the Spring Boot Gradle plugin's own classes
// aren't resolvable here even though they are in the applying build.gradle.kts.
tasks.withType<JavaExec>().matching { it.name == "bootRun" }.configureEach {
    doFirst {
        val envFile = file("${rootProject.projectDir}/.env")
        if (envFile.exists()) {
            println("🔐 Loading environment variables from .env")
            envFile.readLines().forEach { line ->
                if (line.isNotBlank() && !line.trimStart().startsWith("#") && line.contains("=")) {
                    val (key, value) = line.split("=", limit = 2)
                    val envKey = key.trim()
                    var envValue = value.trim()
                    // Remove quotes if present
                    if ((envValue.startsWith("\"") && envValue.endsWith("\"")) ||
                        (envValue.startsWith("'") && envValue.endsWith("'"))) {
                        envValue = envValue.substring(1, envValue.length - 1)
                    }
                    // Only set if not already in environment
                    if (System.getenv(envKey) == null) {
                        environment(envKey, envValue)
                    }
                }
            }
        } else {
            println("ℹ️  .env file not found. Using default configuration values.")
        }
    }
}
