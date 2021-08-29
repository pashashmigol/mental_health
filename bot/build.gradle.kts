import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.appengine")
    id("com.google.gms.google-services")
    war
}

group = "me.pashashmigol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/ktor")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.soywiz.korlibs.klock:klock:2.0.0-alpha")
    implementation("io.ktor:ktor-server-servlet:1.4.0")
    implementation("com.google.api-client:google-api-client:1.30.10")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.30.6")
    implementation("com.google.apis:google-api-services-sheets:v4-rev581-1.25.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev165-1.25.0")
    implementation("com.google.appengine:appengine-api-1.0-sdk:1.9.76")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.20.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.ktor:ktor-gson:1.4.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.danilopianini:gson-extras:0.2.2")
    implementation("com.google.firebase:firebase-admin:7.0.0")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.1")
    implementation("com.soywiz.korlibs.klock:klock:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
    implementation("com.github.nwillc:ksvg:3.0.0")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")

    implementation("net.sf.cssbox:pdf2dom:1.8")
    implementation("com.itextpdf:itextpdf:5.5.10")
    implementation("com.itextpdf.tool:xmlworker:5.5.10")
    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation("org.apache.poi:poi-ooxml:3.15")
    implementation("org.apache.poi:poi-scratchpad:3.15")

    implementation("org.apache.xmlgraphics:batik-codec:1.9")

    implementation("org.apache.pdfbox:pdfbox-tools:2.0.3")
    implementation("net.sf.cssbox:pdf2dom:1.6")

    implementation("org.xhtmlrenderer:flying-saucer-parent:9.1.22")

    implementation("com.itextpdf:itext-asian:5.2.0")
    implementation("com.itextpdf:itextpdf:5.5.13.2")
    implementation("org.xhtmlrenderer:flying-saucer-core:9.1.22")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.1.22")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.14")

    implementation("org.kodein.di:kodein-di:7.6.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
}

appengine {
    deploy {
        version = "GCLOUD_CONFIG"
        projectId = "GCLOUD_CONFIG"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks {
    registering {
        doLast {
            sourceSets.forEach { srcSet ->
                println("[" + srcSet.name + "]")
                print("-->Source directories: " + srcSet.allJava.srcDirs + "\n")
                print("-->Resources directories: " + srcSet.resources.srcDirs + "\n")
                print("-->Output directories: " + srcSet.output.classesDirs.files + "\n")
                println("")
            }
        }
    }
}
