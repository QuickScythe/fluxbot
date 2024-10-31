import org.objectweb.asm.*

import java.nio.file.Files

val jfreeVersion: String by project

val sparkVersion: String by project
val sqlVersion: String by project
val junitVersion: String by project
val logbackVersion: String by project
val jdaVersion: String by project
val group: String by project
val version: String by project
val main: String by project
val asmVersion: String by project

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
    implementation("org.ow2.asm:asm:${asmVersion}")


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

tasks.register("modifyJavaField") {
    doLast {
        val classFile = file("build/classes/java/main/me/quickscythe/vanillaFlux/Bot.class")
        val classReader = ClassReader(Files.newInputStream(classFile.toPath()))
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        classReader.accept(object : ClassVisitor(Opcodes.ASM9, classWriter) {
            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                if (name == "onInitialize") { // Target the specific method
                    return object : MethodVisitor(Opcodes.ASM9, mv) {
                        override fun visitCode() {
                            super.visitCode()
                            // Inject code at the beginning of the method
                            mv.visitVarInsn(Opcodes.ALOAD, 0)
                            mv.visitLdcInsn(false)
                            mv.visitFieldInsn(Opcodes.PUTFIELD, "me/quickscythe/vanillaflux/Initializer", "DEBUG", "Ljava/lang/Boolean;")
                        }
                    }
                }
                return mv
            }
        }, 0)

        Files.write(classFile.toPath(), classWriter.toByteArray())
    }
}

tasks.named("jar") {
    dependsOn("modifyJavaField")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}