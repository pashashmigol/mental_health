buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        classpath("com.android.tools.build:gradle:4.0.2")
        classpath("com.google.cloud.tools:appengine-gradle-plugin:2.2.0")
        classpath("com.google.gms:google-services:4.3.4")
    }
}
group = "me.pashashmigol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
