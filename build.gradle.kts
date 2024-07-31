plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.quickscythe"
version = "1.0-SNAPSHOT"
application.mainClass = "me.quickscythe.vanillaflux.Bot" //
version = "1.0"

val jdaVersion = "5.0.1" //


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")


    implementation("mysql", "mysql-connector-java", "8.0.28");

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