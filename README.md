# Koremods - Launchwrapper

Launchwrapper frontend for [Koremods](https://gitlab.com/gofancy/koremods/koremods), a bytecode modification framework running KTS/JSR-223.


Supported Minecraft versions: 1.12.2

### Usage

Make sure you are running ForgeGradle 3 or higher. This is required for scripts to load in dev.

Declare the dependency in your build.gradle, replacing `<version>` with the desired version
```groovy
dependencies {
    implementation group: 'dev.su5ed.koremods', name: 'koremods-launchwrapper', version: '<version>'
}
```
