import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("net.metalbrain.paysmart.sandbox.app.SandboxServerMain")
}

dependencies {
    implementation(libs.stripe.java)
}

fun String?.cleanOrNull(): String? {
    val clean = this?.trim().orEmpty()
    return clean.takeIf { it.isNotEmpty() }
}

val sandboxLocalFile = layout.projectDirectory.file("local.env.properties").asFile!!
val sandboxLocalProps = Properties().apply {
    if (sandboxLocalFile.exists()) {
        sandboxLocalFile.inputStream().use(::load)
    }
}

fun resolveSandboxValue(
    project: Project,
    gradleKeys: List<String>,
    localKeys: List<String> = gradleKeys,
    envKey: String? = null
): String? {
    val fromGradle = gradleKeys.asSequence()
        .map { key -> project.findProperty(key) as? String }
        .map { value -> value.cleanOrNull() }
        .firstOrNull { value -> value != null }
    if (fromGradle != null) return fromGradle

    val fromLocal = localKeys.asSequence()
        .map { key -> sandboxLocalProps.getProperty(key).cleanOrNull() }
        .firstOrNull { value -> value != null }
    if (fromLocal != null) return fromLocal

    return envKey?.let { key -> System.getenv(key).cleanOrNull() }
}

val runtimeProperties = buildMap {
    resolveSandboxValue(
        project = project,
        gradleKeys = listOf("sandbox.stripeSecretKey", "SANDBOX_STRIPE_SECRET_KEY"),
        localKeys = listOf("SANDBOX_STRIPE_SECRET_KEY", "STRIPE_SECRET_KEY"),
        envKey = "STRIPE_SECRET_KEY"
    )?.let { put("STRIPE_SECRET_KEY", it) }

    resolveSandboxValue(
        project = project,
        gradleKeys = listOf("sandbox.productName", "SANDBOX_PRODUCT_NAME"),
        localKeys = listOf("SANDBOX_PRODUCT_NAME"),
        envKey = "SANDBOX_PRODUCT_NAME"
    )?.let { put("SANDBOX_PRODUCT_NAME", it) }

    resolveSandboxValue(
        project = project,
        gradleKeys = listOf("sandbox.productDescription", "SANDBOX_PRODUCT_DESCRIPTION"),
        localKeys = listOf("SANDBOX_PRODUCT_DESCRIPTION"),
        envKey = "SANDBOX_PRODUCT_DESCRIPTION"
    )?.let { put("SANDBOX_PRODUCT_DESCRIPTION", it) }

    resolveSandboxValue(
        project = project,
        gradleKeys = listOf("sandbox.priceCurrency", "SANDBOX_PRICE_CURRENCY"),
        localKeys = listOf("SANDBOX_PRICE_CURRENCY"),
        envKey = "SANDBOX_PRICE_CURRENCY"
    )?.let { put("SANDBOX_PRICE_CURRENCY", it) }

    resolveSandboxValue(
        project = project,
        gradleKeys = listOf("sandbox.priceMinor", "SANDBOX_PRICE_MINOR"),
        localKeys = listOf("SANDBOX_PRICE_MINOR"),
        envKey = "SANDBOX_PRICE_MINOR"
    )?.let { put("SANDBOX_PRICE_MINOR", it) }
}

tasks.named<JavaExec>("run") {
    systemProperties(runtimeProperties)
}

val sandboxRunRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName == "run" ||
        taskName == ":sandbox:run" ||
        taskName.endsWith(":sandbox:run")
}

if (sandboxRunRequested && !runtimeProperties.containsKey("STRIPE_SECRET_KEY")) {
    throw GradleException(
        """
        Missing Stripe secret for :sandbox:run.
        Provide one of:
        1) sandbox/local.env.properties with SANDBOX_STRIPE_SECRET_KEY=...
        2) -Psandbox.stripeSecretKey=...
        3) STRIPE_SECRET_KEY environment variable
        """.trimIndent()
    )
}
