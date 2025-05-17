plugins {
    id("java")
    kotlin("jvm")
}

group = "grely"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
    maven { url = uri("https://www.jitpack.io") }
    mavenCentral()
}

dependencies {
    implementation("com.github.Anuken.Arc:arc-core:v146")
    implementation("com.discord4j:discord4j-core:3.3.0-SNAPSHOT")
    implementation(files("postgresql-42.7.5.jar"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.jsoup:jsoup:1.20.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.1")

    compileOnly("org.projectlombok:lombok:1.18.38")

    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    archiveFileName.set("kostyl.jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    manifest {
        attributes(
            "Main-Class" to "icu.grely.Main"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
kotlin {
    jvmToolchain(17)
}