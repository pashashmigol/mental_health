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
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
