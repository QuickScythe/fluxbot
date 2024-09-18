val jfreeVersion: String by project

val sparkVersion: String by project
val sqlVersion: String by project
val junitVersion: String by project
val logbackVersion: String by project
val jdaVersion: String by project
val group: String by project
val version: String by project
val main: String by project

plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

application.mainClass = main // Set the main class for the application




repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.sparkjava:spark-core:$sparkVersion")
    implementation("mysql", "mysql-connector-java", sqlVersion)
    implementation("org.jfree:jfreechart:${jfreeVersion}")


//    api group: 'mysql', name: 'mysql-connector-java', version: '8.0.28'

}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of java you want to use,
    // the minimum required for JDA is 1.8
    sourceCompatibility = "20"
}
tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}