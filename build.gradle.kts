plugins {
    id("java")
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
    implementation("com.github.Anuken.Arc:arc-core:v147.1")
    implementation("com.discord4j:discord4j-core:3.3.0-SNAPSHOT")
    implementation(files("postgresql-42.7.5.jar"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    compileOnly("org.projectlombok:lombok:1.18.38")

    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "icu.grely.Main.main"
        )
    }
}
