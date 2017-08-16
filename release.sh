#!/usr/bin/env bash

./gradlew clean build generatePomFileForMavenPublication

read -p 'bintrayUser: ' bintrayUser
read -p 'bintrayKey: ' -s bintrayKey

./gradlew :prefman-annotations:bintrayUpload -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -PdryRun=false
./gradlew :prefman-compiler:bintrayUpload -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -PdryRun=false
./gradlew :prefman:bintrayUpload -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -PdryRun=false