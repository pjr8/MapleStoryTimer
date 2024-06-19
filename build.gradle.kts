plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.paulrobinson"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("javazoom:jlayer:1.0.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "me.paulrobinson.MapleStoryTimer"
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "me.paulrobinson.MapleStoryTimer"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}