/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

plugins {
    id("maven-publish")
    id("signing")
    id("bundle")
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Bundle core library directly as we only get to publish one jar per plugin in Gradle Plugin Portal
    bundleInside(project(":core"))
    implementation(platform(libs.okhttp.bom))
    implementation(libs.google.cloud.storage)
    implementation(libs.google.protobuf.java)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.google.gson)
    implementation(libs.okhttp)
}

gradlePlugin {
    website = "https://github.com/androidx/gcp-gradle-build-cache"
    vcsUrl = "https://github.com/androidx/gcp-gradle-build-cache"
    plugins {
        create("gcpbuildcache") {
            id = "androidx.build.gradle.gcpbuildcache"
            displayName = "Gradle GCP Build Cache Plugin"
            description = """
                - Performance optimization for pulling large remote cache entries, see
                https://github.com/androidx/gcp-gradle-build-cache/pull/40
            """.trimIndent()
            implementationClass = "androidx.build.gradle.gcpbuildcache.GcpGradleBuildCachePlugin"
            tags = listOf("buildcache", "gcp", "caching")
        }
    }
}

group = "androidx.build.gradle.gcpbuildcache"
version = "1.0.0-beta07"

testing {
    suites {
        // Configure built-in test suite.
        val test by getting(JvmTestSuite::class) {
            useJUnit()
        }

        // Create a new functional test suite.
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnit()

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.hasProperty("signing.keyId") }
}
