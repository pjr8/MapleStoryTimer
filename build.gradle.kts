plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("edu.sc.seis.launch4j") version "3.0.5"
}

group = "me.paulrobinson"
version = "1.3"

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

launch4j {
    mainClassName = "com.example.myapp.Start"
    mainClassName = "me.paulrobinson.MapleStoryTimer"
    icon = "${projectDir}/src/main/resources/maplestorytimer.ico"
}

tasks.createExe {
    dependsOn(tasks.shadowJar)
}

tasks.createAllExecutables {
    dependsOn(tasks.shadowJar)
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