#!/usr/bin/env bash
pwd
chmod -R 777 .
mkdir .gradle
ls -la
./gradlew release -i -s -Prelease.useAutomaticVersion=true --gradle-user-home=/home/app/.gradle
NEW_VERSION=$(git describe --abbrev=0)
sed "/appVersion/c\\appVersion: '${NEW_VERSION}'" Chart.yaml > tempChart.yaml && mv tempChart.yaml Chart.yaml
git add . && git commit -m "Set chart version to ${NEW_VERSION} [CI SKIP]" && git push
