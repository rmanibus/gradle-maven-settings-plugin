/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.linguica.gradle.maven.settings

import org.gradle.mvn3.org.apache.maven.settings.Mirror
import org.junit.Test

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

class MavenSettingsPluginTest extends AbstractMavenSettingsTest {

    @Test
    void applyMavenSettingsPlugin() {
        project.with {
            apply plugin: 'net.linguica.maven-settings'
        }

        assertTrue(project.plugins.hasPlugin(MavenSettingsPlugin.class))
    }

    @Test
    void declareGlobalMirror() {
        withSettings {
            mirrors.add new Mirror(id: 'myrepo', mirrorOf: '*', url: 'http://maven.foo.bar')
        }

        addPluginWithSettings(project)

        project.with {
            repositories {
                mavenLocal()
                jcenter()
                mavenCentral()
            }
        }

        project.evaluate()

        assertThat(project.repositories, hasSize(2))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myrepo'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('MavenLocal'))))
    }

    @Test
    void declareExternalMirrorWithFileRepo() {
        withSettings {
            mirrors.add new Mirror(id: 'myrepo', mirrorOf: 'external:*', url: 'http://maven.foo.bar')
        }

        addPluginWithSettings(project)

        project.with {
            repositories {
                mavenLocal()
                jcenter()
                mavenCentral()
                maven {
                    name 'myLocal'
                    url "file://${project.buildDir}/.m2"
                }
            }
        }

        project.evaluate()

        assertThat(project.repositories, hasSize(3))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myrepo'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('MavenLocal'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myLocal'))))
    }

    @Test
    void declareExternalMirrorWithLocalhostRepo() {
        withSettings {
            mirrors.add new Mirror(id: 'myrepo', mirrorOf: 'external:*', url: 'http://maven.foo.bar')
        }

        addPluginWithSettings(project)

        project.with {
            repositories {
                mavenLocal()
                jcenter()
                mavenCentral()
                maven {
                    name 'myLocal'
                    url "http://localhost/maven"
                }
            }
        }

        project.evaluate()

        assertThat(project.repositories, hasSize(3))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myrepo'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('MavenLocal'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myLocal'))))
    }

    @Test
    void declareMavenCentralMirror() {
        withSettings {
            mirrors.add new Mirror(id: 'myrepo', mirrorOf: 'central', url: 'http://maven.foo.bar')
        }

        addPluginWithSettings(project)

        project.with {
            repositories {
                mavenLocal()
                mavenCentral()
                maven {
                    name 'myRemote'
                    url "https://maven.foobar.org/repo"
                }
            }
        }

        project.evaluate()

        assertThat(project.repositories, hasSize(3))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myrepo'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('MavenLocal'))))
        assertThat(project.repositories, hasItem(hasProperty('name', equalTo('myRemote'))))
    }
}