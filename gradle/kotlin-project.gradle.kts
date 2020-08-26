rootProject.extensions.configure<ExtraPropertiesExtension>("ext") {
    val libs: (String) -> String by (rootProject.extensions.getByName("ext") as ExtraPropertiesExtension)

    val function: Project.() -> Unit = {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        configureKtlint()

        dependencies {
            add("testImplementation", libs("kotlinx-coroutines-jdk8"))
            add("testImplementation", libs("kotlinx-coroutines-core"))
            add("testImplementation", libs("kotlin-stdlib-jdk8"))
        }
    }
    set("configureKotlin", function)
}

fun Project.configureKtlint() {
    configurations.create("ktlint")
    dependencies {
        val ktlintVersion = "0.36.0"
        "ktlint"("com.pinterest:ktlint:$ktlintVersion")
    }

    val ktlintTask = tasks.register<JavaExec>("ktlint") {
        group = "verification"
        description = "Check Kotlin code style"
        classpath = configurations["ktlint"]
        main = "com.pinterest.ktlint.Main"
        args("src/main/**/*.kt", "src/test/**/*.kt")
    }

    tasks.named("check").configure { dependsOn(ktlintTask) }

    tasks.register<JavaExec>("ktlintFormat") {
        group = "formatting"
        description = "Fix Kotlin code style deviations"
        classpath = configurations["ktlint"]
        main = "com.pinterest.ktlint.Main"
        args("-F", "src/main/**/*.kt", "src/test/**/*.kt")
    }
}